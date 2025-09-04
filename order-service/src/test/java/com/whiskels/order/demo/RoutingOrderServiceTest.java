package com.whiskels.order.demo;

import com.whiskels.order.api.dto.CreateOrderRequest;
import com.whiskels.order.api.dto.CreateOrderResponse;
import com.whiskels.order.api.service.BasicOrderService;
import com.whiskels.order.api.service.outbox.OutboxOrderService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoutingOrderServiceTest {

    @Mock
    private BasicOrderService basicOrderService;

    @Mock
    private OutboxOrderService outboxOrderService;

    @Mock
    private SimulationStrategyProvider simulationStrategyProvider;

    @InjectMocks
    private RoutingOrderService routingOrderService;

    @Test
    @DisplayName("Should use outbox service when strategy is OUTBOX")
    void shouldUseOutboxServiceWhenStrategyIsOutbox() {
        // Given
        CreateOrderRequest request = mock(CreateOrderRequest.class);
        CreateOrderResponse expectedResponse = mock(CreateOrderResponse.class);

        when(simulationStrategyProvider.get()).thenReturn(SimulationStrategyEnum.OUTBOX);
        when(outboxOrderService.create(request)).thenReturn(expectedResponse);

        // When
        CreateOrderResponse result = routingOrderService.create(request);

        // Then
        assertEquals(expectedResponse, result);
        verify(outboxOrderService).create(request);
        verify(basicOrderService, never()).create(any());
    }

    @Test
    @DisplayName("Should use basic service and throw exception when strategy is FAILED_COMMIT_ANOMALY")
    void shouldThrowExceptionWhenStrategyIsFailedCommitAnomaly() {
        // Given
        CreateOrderRequest request = mock(CreateOrderRequest.class);
        CreateOrderResponse response = mock(CreateOrderResponse.class);

        when(simulationStrategyProvider.get()).thenReturn(SimulationStrategyEnum.FAILED_COMMIT_ANOMALY);
        when(basicOrderService.create(request)).thenReturn(response);

        // When/Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> routingOrderService.create(request));

        assertEquals("Simulated failure before commit", exception.getMessage());
        verify(basicOrderService).create(request);
        verify(outboxOrderService, never()).create(any());
    }

    @ParameterizedTest
    @EnumSource(value = SimulationStrategyEnum.class, mode = EnumSource.Mode.EXCLUDE, names = {"OUTBOX", "FAILED_COMMIT_ANOMALY"})
    @DisplayName("Should use basic service and return response when strategy is {}}")
    void shouldUseBasicServiceOnOtherStrategiesWithNoExceptions(SimulationStrategyEnum strategy) {
        // Given
        CreateOrderRequest request = mock(CreateOrderRequest.class);
        CreateOrderResponse expectedResponse = mock(CreateOrderResponse.class);

        when(simulationStrategyProvider.get()).thenReturn(SimulationStrategyEnum.NONE);
        when(basicOrderService.create(request)).thenReturn(expectedResponse);

        // When
        CreateOrderResponse result = routingOrderService.create(request);

        // Then
        assertEquals(expectedResponse, result);
        verify(basicOrderService).create(request);
        verify(outboxOrderService, never()).create(any());
    }
}