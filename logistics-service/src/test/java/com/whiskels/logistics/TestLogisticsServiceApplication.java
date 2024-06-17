package com.whiskels.logistics;

import org.springframework.boot.SpringApplication;

public class TestLogisticsServiceApplication {

    public static void main(String[] args) {
        SpringApplication.from(LogisticsServiceApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
