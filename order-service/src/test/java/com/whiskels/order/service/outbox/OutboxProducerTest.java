package com.whiskels.order.service.outbox;

import com.whiskels.order.entity.OutboxEvent;
import com.whiskels.order.repository.OutboxEventRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OutboxProducerTest {
    @Mock
    private OutboxEventRepository outboxEventRepository;

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @InjectMocks
    private OutboxProducer outboxProducer;

    @ParameterizedTest
    @MethodSource("testSend_FailureShouldNotUpdate")
    void testSend_FailureShouldNotUpdate(CompletableFuture<SendResult<String, String>> failedFuture)  {
        given(outboxEventRepository.findNotSentBatch()).willReturn(List.of(new OutboxEvent()));
        given(kafkaTemplate.send(any(), any())).willReturn(failedFuture);

        outboxProducer.send();
        try {
            failedFuture.get();
        } catch (Exception e) {
            // ignore
        }

        verify(outboxEventRepository, never()).save(any());
    }

    static Stream<Arguments> testSend_FailureShouldNotUpdate() {
        return Stream.of(
                Arguments.of(CompletableFuture.failedFuture(new RuntimeException())),
                Arguments.of(CompletableFuture.completedFuture(null))
        );
    }

    @Test
    void testSend_SuccessShouldUpdate()  {
        given(outboxEventRepository.findNotSentBatch()).willReturn(List.of(new OutboxEvent()));
        CompletableFuture<SendResult<String, String>> failedFuture = CompletableFuture.completedFuture(new SendResult<>(null, null));
        given(kafkaTemplate.send(any(), any())).willReturn(failedFuture);

        outboxProducer.send();
        try {
            failedFuture.get();
        } catch (Exception e) {
            // ignore
        }

        verify(outboxEventRepository).save(any());
    }
}
