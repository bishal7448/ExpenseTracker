package com.example.UserService.deserializer;

import com.example.UserService.models.UserInfoDto;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.serialization.Deserializer;
import org.springframework.kafka.support.serializer.DeserializationException;
import tools.jackson.databind.ObjectMapper;

import java.nio.ByteBuffer;
import java.util.Map;

public class UserInfoDeserializer implements Deserializer<UserInfoDto> {
    private final static ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public UserInfoDto deserialize(String key, byte[] eventData) {
        if(eventData == null) {
            return null;
        }

        try {
            return objectMapper.readValue(eventData, UserInfoDto.class);
        } catch (Exception e) {
            throw new DeserializationException(
                    "Error while trying to deserialize",
                    eventData,
                    false, // false because this is the VALUE, not the KEY
                    e
            );
        }
    }

    @Override
    public UserInfoDto deserialize(String topic, Headers headers, ByteBuffer data) {
        return Deserializer.super.deserialize(topic, headers, data);
    }

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        Deserializer.super.configure(configs, isKey);
    }

    @Override
    public void close() {
        Deserializer.super.close();
    }

    @Override
    public UserInfoDto deserialize(String topic, Headers headers, byte[] data) {
        return Deserializer.super.deserialize(topic, headers, data);
    }
}

