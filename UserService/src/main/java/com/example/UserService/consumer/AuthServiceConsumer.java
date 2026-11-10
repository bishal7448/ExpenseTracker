package com.example.UserService.consumer;

import com.example.UserService.models.UserInfoDto;
import com.example.UserService.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class AuthServiceConsumer {

    @Autowired
    private UserService userService;

    @KafkaListener(
            topics = "${spring.kafka.topic.name}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consume(UserInfoDto eventData) {
        try {
            userService.createOrUpdateUser(eventData);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

