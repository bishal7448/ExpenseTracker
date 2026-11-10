package com.example.UserService.models;

import com.example.UserService.entities.UserInfo;
import lombok.*;
import com.fasterxml.jackson.annotation.*;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class UserInfoDto {

    @NonNull
    private String userId;

    @NonNull
    private String firstName;

    @NonNull
    private String lastName;

    @NonNull
    private String email;

    @NonNull
    private Long phoneNumber;

    private String profilePicture;

    public UserInfo transformToUserInfo() {
        return UserInfo.builder()
                .userId(this.userId)
                .firstName(this.firstName)
                .lastName(this.lastName)
                .email(this.email)
                .phoneNumber(this.phoneNumber)
                .profilePicture(this.profilePicture).build();
    }
}

