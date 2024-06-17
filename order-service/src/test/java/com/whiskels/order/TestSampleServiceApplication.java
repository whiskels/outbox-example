package com.whiskels.order;

import org.springframework.boot.SpringApplication;

public class TestSampleServiceApplication {

    public static void main(String[] args) {
        SpringApplication.from(OrderServiceApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
