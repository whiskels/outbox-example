package com.whiskels.order.api.controller;

import com.whiskels.order.BaseIT;
import com.whiskels.order.api.dto.CreateOrderRequest;
import com.whiskels.order.api.dto.CreateOrderResponse;
import com.whiskels.order.demo.SimulationStrategyEnum;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

import static org.awaitility.Awaitility.await;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class OrderControllerIT extends BaseIT {

    @ParameterizedTest
    @DisplayName("Should persist order and send it to kafka using strategy")
    @EnumSource(value = SimulationStrategyEnum.class, names = {"NONE", "OUTBOX"}, mode = EnumSource.Mode.INCLUDE)
    void shouldCreateOrderSuccessfully(SimulationStrategyEnum strategy) throws Exception {
        CreateOrderRequest request = createOrderRequest();

        // When
        MvcResult result = postOrder(request, strategy)
                // Then
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.userId").value(request.getUserId().toString()))
                .andReturn();

        CreateOrderResponse response = MAPPER.readValue(
                result.getResponse().getContentAsString(), CreateOrderResponse.class);

        await().atMost(Duration.ofSeconds(10))
                .untilAsserted(() -> testConsumer.hasMessageForUser(response.getUserId()));
    }

    @ParameterizedTest
    @DisplayName("Should persist order, but fail to send it to kafka using strategy")
    @EnumSource(value = SimulationStrategyEnum.class, names = {"FAILED_BROKER_ANOMALY"}, mode = EnumSource.Mode.INCLUDE)
    void shouldCreateOrderSuccessfullyButFailToSend(SimulationStrategyEnum strategy) throws Exception {
        CreateOrderRequest request = createOrderRequest();

        // When
        MvcResult result = postOrder(request, strategy)
                // Then
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.userId").value(request.getUserId().toString()))
                .andReturn();

        CreateOrderResponse response = MAPPER.readValue(
                result.getResponse().getContentAsString(), CreateOrderResponse.class);

        Thread.sleep(3000); // Wait to ensure no message is sent
        Assertions.assertFalse(testConsumer.hasMessageForUser(response.getUserId()));


    }

    @ParameterizedTest
    @DisplayName("Should fail to persist order, but send it to kafka using strategy")
    @EnumSource(value = SimulationStrategyEnum.class, names = {"FAILED_COMMIT_ANOMALY"}, mode = EnumSource.Mode.INCLUDE)
    void shouldSendOrderSuccessfullyButFailToPersist(SimulationStrategyEnum strategy) throws Exception {
        CreateOrderRequest request = createOrderRequest();

        // When
        postOrder(request, strategy)
                // Then
                .andExpect(status().is5xxServerError());


        await().atMost(Duration.ofSeconds(10))
                .untilAsserted(() -> testConsumer.hasMessageForUser(request.getUserId()));
    }

    private CreateOrderRequest createOrderRequest() {
        return new CreateOrderRequest(
                UUID.randomUUID(),
                List.of(new CreateOrderRequest.OrderItemDto(UUID.randomUUID(), 1))
        );
    }

    private ResultActions postOrder(CreateOrderRequest request, SimulationStrategyEnum strategy) throws Exception {
        return mvc.perform(post("/orders")
                .header(SimulationStrategyEnum.STRATEGY_HEADER_NAME, strategy.name())
                .contentType(MediaType.APPLICATION_JSON)
                .content(MAPPER.writeValueAsString(request)));
    }
}
