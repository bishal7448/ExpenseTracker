package org.example.producer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.support.SendResult;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class UserInfoProducer {

    private final KafkaTemplate<String, UserInfoEvent> kafkaTemplate;

    @Value("${spring.kafka.topic.name}")
    private String topicName;

    public UserInfoProducer(KafkaTemplate<String, UserInfoEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public CompletableFuture<SendResult<String, UserInfoEvent>> sendEventToKafka(UserInfoEvent eventData) {
        log.info("Publishing UserInfoEvent for userId: {} to topic: {}", eventData.getUserId(), topicName);

        CompletableFuture<SendResult<String, UserInfoEvent>> future = kafkaTemplate.send(topicName, eventData.getUserId(), eventData);
        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to send UserInfoEvent to Kafka topic {} for userId {}: {}",
                        topicName, eventData.getUserId(), ex.getMessage(), ex);
            } else {
                log.info("Successfully sent UserInfoEvent for userId: {} to topic: {} [partition: {}, offset: {}]",
                        eventData.getUserId(),
                        topicName,
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            }
        });

        return future;
    }
}
