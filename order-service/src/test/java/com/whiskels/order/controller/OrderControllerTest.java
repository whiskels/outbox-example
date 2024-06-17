package com.whiskels.order.controller;

import com.whiskels.order.SimulationStrategyEnum;
import com.whiskels.order.dto.OrderCreatedDto;
import com.whiskels.order.dto.OrderDto;
import com.whiskels.order.service.SimulatedOrderService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
@Import(OrderControllerTest.SimulatedOrderServiceConfig.class)
public class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SimulatedOrderService simulatedOrderService;

    @Test
    public void testCreateOrder() throws Exception {
        OrderCreatedDto orderCreatedDto = new OrderCreatedDto();
        orderCreatedDto.setId(UUID.randomUUID());
        orderCreatedDto.setUserId(UUID.randomUUID());

        when(simulatedOrderService.create(any(OrderDto.class))).thenReturn(orderCreatedDto);

        OrderDto orderDto = new OrderDto();
        orderDto.setUserId(UUID.randomUUID());

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\": \"e7b0f8a8-03e8-4eb6-8588-3e51a94b5f36\", \"items\": []}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderCreatedDto.getId().toString()))
                .andExpect(jsonPath("$.userId").value(orderCreatedDto.getUserId().toString()));
    }

    @TestConfiguration
    static class SimulatedOrderServiceConfig {

        @Bean
        SimulatedOrderService simulatedOrderService() {
            var bean = Mockito.mock(SimulatedOrderService.class);
            when(bean.strategy()).thenReturn(SimulationStrategyEnum.OUTBOX);
            return bean;
        }
    }
}