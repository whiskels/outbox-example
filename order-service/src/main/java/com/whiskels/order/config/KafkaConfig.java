package com.whiskels.order.config;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.context.properties.PropertyMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.ProducerListener;
import org.springframework.kafka.support.converter.RecordMessageConverter;

import java.util.Collections;
import java.util.Map;

@Configuration(proxyBeanMethods = false)
class KafkaConfig {

    @Bean
    KafkaTemplate<String, String> kafkaTemplate(
            ProducerFactory<String, String> kafkaProducerFactory,
            ProducerListener<Object, Object> kafkaProducerListener,
            ObjectProvider<RecordMessageConverter> messageConverter,
            KafkaProperties properties) {

        return createKafkaTemplate(kafkaProducerFactory, kafkaProducerListener, messageConverter,
                properties, Collections.emptyMap());
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private <T> KafkaTemplate<String, T> createKafkaTemplate(ProducerFactory<String, T> kafkaProducerFactory,
                                                             ProducerListener<Object, Object> kafkaProducerListener, ObjectProvider<RecordMessageConverter> messageConverter,
                                                             KafkaProperties properties, Map<String, Object> configurationOverrides) {
        PropertyMapper map = PropertyMapper.get().alwaysApplyingWhenNonNull();
        KafkaTemplate<String, T> kafkaTemplate = new KafkaTemplate<>(kafkaProducerFactory, configurationOverrides);
        messageConverter.ifUnique(kafkaTemplate::setMessageConverter);
        map.from(kafkaProducerListener).to(((KafkaTemplate) kafkaTemplate)::setProducerListener);
        map.from(properties.getTemplate().getDefaultTopic()).to(kafkaTemplate::setDefaultTopic);
        map.from(properties.getTemplate().getTransactionIdPrefix()).to(kafkaTemplate::setTransactionIdPrefix);
        map.from(properties.getTemplate().isObservationEnabled()).to(kafkaTemplate::setObservationEnabled);
        return kafkaTemplate;
    }
}
