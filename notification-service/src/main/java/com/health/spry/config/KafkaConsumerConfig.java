package com.health.spry.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import com.health.spry.kafka.BookNotificationEvent;

@Configuration
@EnableKafka
public class KafkaConsumerConfig {

//    @Value("${spring.kafka.bootstrap-servers}")
//    private String bootstrapServers;
//
//    @Value("${spring.kafka.consumer.group-id}")
//    private String groupId;
//
//    @Bean
//    public ConsumerFactory<String, BookNotificationEvent> consumerFactory() {
//        Map<String, Object> configProps = new HashMap<>();
//        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
//        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
//        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
//        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
//        configProps.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
//        configProps.put(JsonDeserializer.VALUE_DEFAULT_TYPE, BookNotificationEvent.class.getName());
//        configProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
//        
//        
//        return new DefaultKafkaConsumerFactory<>(
//                configProps,
//                new StringDeserializer(),
//                new JsonDeserializer<>(BookNotificationEvent.class, false)
//        );
//    }
//
//    @Bean
//    public ConcurrentKafkaListenerContainerFactory<String, BookNotificationEvent> kafkaListenerContainerFactory() {
//        ConcurrentKafkaListenerContainerFactory<String, BookNotificationEvent> factory =
//                new ConcurrentKafkaListenerContainerFactory<>();
//        factory.setConsumerFactory(consumerFactory());
//        return factory;
//    }
}
