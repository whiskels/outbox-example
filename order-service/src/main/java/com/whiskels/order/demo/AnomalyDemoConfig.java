package com.whiskels.order.demo;

import com.whiskels.order.api.service.BasicOrderService;
import com.whiskels.order.api.service.OrderService;
import com.whiskels.order.api.service.outbox.OutboxOrderService;
import io.swagger.v3.oas.models.media.StringSchema;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.util.Random;
import java.util.concurrent.CompletableFuture;

@Configuration
class AnomalyDemoConfig {
    @Bean
    Random random() {
        return new Random();
    }

    @Bean
    SimulationStrategyProvider simulationStrategyFilter(Random random) {
        return new SimulationStrategyProvider(random);
    }

    @Bean
    @Primary
    OrderService routingOrderService(@Lazy BasicOrderService basicOrderService,
                                     @Lazy OutboxOrderService outboxOrderService,
                                     SimulationStrategyProvider simulationStrategyProvider) {
        return new RoutingOrderService(basicOrderService, outboxOrderService, simulationStrategyProvider);
    }

    @Bean
    @Primary
    KafkaTemplate<String, String> flakyTemplate(KafkaTemplate<String, String> delegate, SimulationStrategyProvider simulationStrategyProvider) {
        return new KafkaTemplate<>(delegate.getProducerFactory()) {
            @Override
            public CompletableFuture<SendResult<String, String>> send(String topic, String data) {
                if (simulationStrategyProvider.get() == SimulationStrategyEnum.FAILED_BROKER_ANOMALY) {
                    return CompletableFuture.failedFuture(new DemoAnomalyException("Simulated flaky broker failure"));
                }
                return delegate.send(topic, data);
            }
        };
    }

    @Bean
    OpenApiCustomizer addDemoStrategyHeader() {
        return openApi -> openApi.getPaths().forEach((path, item) -> {
            item.readOperations().forEach(operation -> {
                operation.addParametersItem(
                        new io.swagger.v3.oas.models.parameters.HeaderParameter()
                                .name(SimulationStrategyEnum.STRATEGY_HEADER_NAME)
                                .description("Demo strategy to use")
                                .required(false)
                                .example(SimulationStrategyEnum.OUTBOX.name())
                                .schema(new StringSchema()._default(SimulationStrategyEnum.NONE.name()))
                );
            });
        });
    }
}
