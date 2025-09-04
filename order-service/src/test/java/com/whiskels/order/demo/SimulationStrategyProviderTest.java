package com.whiskels.order.demo;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Random;
import java.util.stream.Stream;

import static com.whiskels.order.demo.SimulationStrategyEnum.FAILED_BROKER_ANOMALY;
import static com.whiskels.order.demo.SimulationStrategyEnum.FAILED_COMMIT_ANOMALY;
import static com.whiskels.order.demo.SimulationStrategyEnum.NONE;
import static com.whiskels.order.demo.SimulationStrategyEnum.STRATEGY_HEADER_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SimulationStrategyProviderTest {
    @Mock
    private Random random;
    @Mock
    private HttpServletResponse response;
    @Mock
    private HttpServletRequest req;

    @InjectMocks
    private SimulationStrategyProvider provider;


    @Test
    @DisplayName("Returns NONE strategy when no strategy is set")
    void returnsNoneStrategyWhenNotSet() {
        SimulationStrategyProvider provider = new SimulationStrategyProvider(new Random());

        assertEquals(NONE, provider.get());
    }

    @ParameterizedTest
    @MethodSource
    @DisplayName("Should use {2} strategy if Random is between {0} and {1} and none strategy provided in request")
    void shouldUseRandomStrategyWhenNoneGiven(int low, int high, SimulationStrategyEnum expected) throws Exception {
        when(req.getHeader(STRATEGY_HEADER_NAME)).thenReturn(null);

        for (int i = low; i <= high; i++) {
            when(random.nextInt(anyInt())).thenReturn(i);
            provider.doFilterInternal(req, response, (request, response) -> {
                assertEquals(expected, provider.get());
            });
        }
    }

    static Stream<Arguments> shouldUseRandomStrategyWhenNoneGiven() {
        return Stream.of(
                Arguments.of(0, 4, FAILED_COMMIT_ANOMALY),
                Arguments.of(5, 9, FAILED_BROKER_ANOMALY),
                Arguments.of(10, 99, NONE)
        );
    }


    @ParameterizedTest
    @EnumSource(value = SimulationStrategyEnum.class)
    @DisplayName("Should extract {} header from request")
    void validateExtractionFromRequestHeader(SimulationStrategyEnum expected) throws Exception {
        when(req.getHeader(STRATEGY_HEADER_NAME)).thenReturn(expected.name());

        provider.doFilterInternal(req, response, (request, response) -> {
            assertEquals(expected, provider.get());
        });

        verify(response).setHeader(STRATEGY_HEADER_NAME, expected.name());
        assertEquals(NONE, provider.get());
    }
}