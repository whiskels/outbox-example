package com.whiskels.logistics.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;


@Service
@Slf4j
@RequiredArgsConstructor
class OrderListener {
    private final OrderRepository orderRepository;

    @KafkaListener(topics = "orders")
    public void listen(Order order) {
        log.info("Received new order event: " + order);
        orderRepository.save(order);
        log.info("Preparing delivery options for the client");
    }

}
