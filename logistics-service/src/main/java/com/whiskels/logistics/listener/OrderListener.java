package com.whiskels.logistics.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;


@Service
@Slf4j
@RequiredArgsConstructor
class OrderListener {
    @KafkaListener(id = "logistics", topics = "orders", clientIdPrefix = "logistics-service")
    public void listen(String data) {
        log.info("Received new order event: " + data);
        log.info("Preparing delivery options for the client");
    }

}
