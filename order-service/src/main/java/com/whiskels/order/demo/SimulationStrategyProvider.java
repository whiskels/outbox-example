package com.whiskels.order.demo;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Objects;
import java.util.Random;

@RequiredArgsConstructor
class SimulationStrategyProvider extends OncePerRequestFilter {
    private static final ThreadLocal<SimulationStrategyEnum> STRATEGY = new ThreadLocal<>();

    private static final int RANDOM_BOUND = 99;
    private static final int FAILED_COMMIT_PROBABILITY = 5;
    private static final int FAILED_BROKER_PROBABILITY = 5;

    private final Random RANDOM;

    public SimulationStrategyEnum get() {
        return Objects.requireNonNullElse(STRATEGY.get(), SimulationStrategyEnum.NONE);
    }

    @Override
    public void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String strategyHeader = request.getHeader(SimulationStrategyEnum.STRATEGY_HEADER_NAME);
            SimulationStrategyEnum strategy = StringUtils.isEmpty(strategyHeader)
                    ? pickRandomStrategy()
                    : parseStrategy(strategyHeader);
            STRATEGY.set(strategy);
            response.setHeader(SimulationStrategyEnum.STRATEGY_HEADER_NAME, strategy.name());
            filterChain.doFilter(request, response);
        } finally {
            STRATEGY.remove();
        }
    }

    private SimulationStrategyEnum pickRandomStrategy() {
        int roll = RANDOM.nextInt(RANDOM_BOUND);
        if (roll < FAILED_COMMIT_PROBABILITY) {
            return SimulationStrategyEnum.FAILED_COMMIT_ANOMALY;
        }
        if (roll < FAILED_BROKER_PROBABILITY + FAILED_COMMIT_PROBABILITY) {
            return SimulationStrategyEnum.FAILED_BROKER_ANOMALY;
        }
        return SimulationStrategyEnum.NONE;
    }

    private SimulationStrategyEnum parseStrategy(String value) {
        try {
            return SimulationStrategyEnum.valueOf(value);
        } catch (IllegalArgumentException e) {
            return SimulationStrategyEnum.NONE;
        }
    }
}
