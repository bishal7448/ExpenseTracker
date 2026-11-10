package com.example.UserService.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class UserInfo {

//    @GeneratedValue(strategy = GenerationType.AUTO)
//    private Long id;

    @Id
    @NonNull
    @JsonProperty("user_id")
    private String userId;

    @NonNull
    @JsonProperty("first_name")
    private String firstName;

    @NonNull
    @JsonProperty("last_name")
    private String lastName;

    @NonNull
    @JsonProperty("phone_number")
    private Long phoneNumber;

    @NonNull
    @JsonProperty("email")
    private String email;

    @JsonProperty("profile_picture")
    private String profilePicture;

}

