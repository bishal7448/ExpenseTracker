package com.example.UserService.services;

import com.example.UserService.entities.UserInfo;
import com.example.UserService.models.UserInfoDto;
import com.example.UserService.repositories.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public UserInfoDto createOrUpdateUser(UserInfoDto userInfoDto) {
        UnaryOperator<UserInfo> updatingUser = user -> {
            user.setFirstName(userInfoDto.getFirstName());
            user.setLastName(userInfoDto.getLastName());
            user.setPhoneNumber(userInfoDto.getPhoneNumber());
            user.setEmail(userInfoDto.getEmail());
            user.setProfilePicture(userInfoDto.getProfilePicture());
            return userRepository.save(user);  // pass-by-reference
        };

        Supplier<UserInfo> createUser = () -> {
            return userRepository.save(userInfoDto.transformToUserInfo());
        };

        UserInfo userInfo = userRepository.findByUserId(userInfoDto.getUserId())
                .map(updatingUser)
                .orElseGet(createUser);

        return UserInfoDto.builder()
                .userId(userInfo.getUserId())
                .firstName(userInfo.getFirstName())
                .lastName(userInfo.getLastName())
                .email(userInfo.getEmail())
                .phoneNumber(userInfo.getPhoneNumber())
                .profilePicture(userInfo.getProfilePicture())
                .build();
    }

    public UserInfoDto getUser(UserInfoDto userInfoDto) throws EntityNotFoundException {
        Optional<UserInfo> userInfoOptional = userRepository.findByUserId(userInfoDto.getUserId());

        if (userInfoOptional.isEmpty()) {
            throw new EntityNotFoundException("User not found");
        }

        UserInfo userInfo = userInfoOptional.get();

        return UserInfoDto.builder()
                .userId(userInfo.getUserId())
                .firstName(userInfo.getFirstName())
                .lastName(userInfo.getLastName())
                .email(userInfo.getEmail())
                .phoneNumber(userInfo.getPhoneNumber())
                .profilePicture(userInfo.getProfilePicture())
                .build();
    }
}

