package com.whiskels.order.demo;

public enum SimulationStrategyEnum {
    OUTBOX,
    FAILED_COMMIT_ANOMALY,
    FAILED_BROKER_ANOMALY,
    NONE;

    public static final String STRATEGY_HEADER_NAME = "X-Simulation-Strategy";

}
