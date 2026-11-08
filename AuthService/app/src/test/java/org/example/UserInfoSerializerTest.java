package org.example;

import org.example.producer.UserInfoEvent;
import org.example.serializer.UserInfoSerializer;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.*;

public class UserInfoSerializerTest {

    @Test
    public void testSerializeUserInfoEvent() {
        UserInfoSerializer serializer = new UserInfoSerializer();
        UserInfoEvent event = UserInfoEvent.builder()
                .userId("u-12345")
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .phoneNumber(9876543210L)
                .build();

        byte[] bytes = serializer.serialize("user_service", event);
        assertNotNull(bytes);

        String json = new String(bytes, StandardCharsets.UTF_8);
        assertTrue(json.contains("\"user_id\":\"u-12345\""));
        assertTrue(json.contains("\"first_name\":\"John\""));
        assertTrue(json.contains("\"last_name\":\"Doe\""));
        assertTrue(json.contains("\"email\":\"john.doe@example.com\""));
        assertTrue(json.contains("\"phone_number\":9876543210"));
    }

    @Test
    public void testSerializeNullData() {
        UserInfoSerializer serializer = new UserInfoSerializer();
        byte[] bytes = serializer.serialize("user_service", null);
        assertNull(bytes);
    }
}

