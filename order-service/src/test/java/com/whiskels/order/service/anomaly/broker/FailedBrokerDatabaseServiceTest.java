package com.whiskels.order.service.anomaly.broker;

import com.whiskels.order.dto.OrderCreatedDto;
import com.whiskels.order.dto.OrderDto;
import com.whiskels.order.entity.Order;
import com.whiskels.order.mapper.OrderMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class FailedBrokerDatabaseServiceTest {

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private CrudRepository<Order, UUID> orderRepository;

    @Mock
    private CorruptedKafkaTemplate kafkaTemplate;

    @InjectMocks
    private FailedBrokerDatabaseService service;

    @Test
    public void testCreate_Successful() {
        OrderDto orderDto = new OrderDto();
        orderDto.setUserId(UUID.randomUUID());
        Order orderEntity = new Order();
        orderEntity.setId(UUID.randomUUID());
        when(orderMapper.toEntity(any(OrderDto.class))).thenReturn(orderEntity);
        when(orderRepository.save(any(Order.class))).thenReturn(orderEntity);

        CompletableFuture future = CompletableFuture.completedFuture(null);
        when(kafkaTemplate.send(anyString(), anyString())).thenReturn(future);

        OrderCreatedDto result = service.create(orderDto);

        verify(orderMapper).toEntity(orderDto);
        verify(orderRepository).save(orderEntity);
        verify(kafkaTemplate).send(eq("orders"), anyString());
    }

    @Test
    public void testCreate_KafkaSendFailure() {
        OrderDto orderDto = new OrderDto();
        orderDto.setUserId(UUID.randomUUID());
        Order orderEntity = new Order();
        orderEntity.setId(UUID.randomUUID());
        when(orderMapper.toEntity(any(OrderDto.class))).thenReturn(orderEntity);
        when(orderRepository.save(any(Order.class))).thenReturn(orderEntity);

        CompletableFuture future = new CompletableFuture();
        future.completeExceptionally(new RuntimeException("Simulated Kafka exception"));
        when(kafkaTemplate.send(anyString(), anyString())).thenReturn(future);

        OrderCreatedDto result = service.create(orderDto);

        verify(orderMapper).toEntity(orderDto);
        verify(orderRepository).save(orderEntity);
        verify(kafkaTemplate).send(eq("orders"), anyString());
    }
}