package com.whiskels.order;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.whiskels.order.api.domain.Order;
import com.whiskels.order.api.domain.OutboxEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.UUID;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("testcontainers")
@Testcontainers
@Import({BaseIT.TestListenerConfig.class, BaseIT.TestcontainersConfiguration.class})
public abstract class BaseIT {
    protected static final ObjectMapper MAPPER = new ObjectMapper();

    @Autowired
    protected JpaRepository<OutboxEvent, UUID> outboxEventRepository;

    @Autowired
    protected JpaRepository<Order, UUID> orderRepository;

    @Autowired
    protected TestConsumer testConsumer;

    @Autowired
    protected MockMvc mvc;

    @BeforeEach
    @AfterEach
    void cleanUp() {
        outboxEventRepository.deleteAll();
        orderRepository.deleteAll();
    }

    @Value("${producer.topic}")
    protected String topicName;

    @TestConfiguration
    static class TestListenerConfig {
        @Bean
        TestConsumer testListener() {
            return new TestConsumer();
        }
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class TestcontainersConfiguration {

        static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(DockerImageName.parse("postgres:16.3"));
        static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.8.2"))
                .withEnv("KAFKA_AUTO_CREATE_TOPICS_ENABLE", "true");

        @Bean
        @ServiceConnection
        PostgreSQLContainer<?> postgresContainer() {
            return postgres;
        }

        @Bean
        @ServiceConnection
        KafkaContainer kafkaContainer() {
            return kafka;
        }

        @DynamicPropertySource
        public static void kafkaProperties(DynamicPropertyRegistry registry) {
            registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
            registry.add("spring.datasource.url", postgres::getJdbcUrl);
            registry.add("spring.datasource.username", postgres::getUsername);
            registry.add("spring.datasource.password", postgres::getPassword);
        }
    }
}
