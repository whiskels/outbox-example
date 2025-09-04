package com.whiskels.order.api.controller;

import com.whiskels.order.api.dto.CreateOrderRequest;
import com.whiskels.order.api.dto.CreateOrderResponse;
import com.whiskels.order.api.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    @Test
    void testCreateOrder() throws Exception {
        CreateOrderResponse createOrderResponse = new CreateOrderResponse(UUID.randomUUID(), UUID.randomUUID());

        when(orderService.create(any(CreateOrderRequest.class))).thenReturn(createOrderResponse);

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\": \"e7b0f8a8-03e8-4eb6-8588-3e51a94b5f36\", \"items\": []}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createOrderResponse.getId().toString()))
                .andExpect(jsonPath("$.userId").value(createOrderResponse.getUserId().toString()));
    }
}