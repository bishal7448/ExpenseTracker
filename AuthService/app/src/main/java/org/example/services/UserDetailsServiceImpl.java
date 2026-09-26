package org.example.services;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.example.entities.UserInfo;
import org.example.models.UserInfoDto;
import org.example.producer.UserInfoProducer;
import org.example.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Objects;
import java.util.UUID;

@Component
@AllArgsConstructor
@Data
@Slf4j
public class UserDetailsServiceImpl implements UserDetailsService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserInfoProducer userInfoProducer;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserInfo user = userRepository.findByUserName(username);
        if(user == null) {
            throw new UsernameNotFoundException("Could not found user..!!");
        }
        return new CustomUserDetails(user);
    }

    public UserInfo checkIfUserAlreadyExist(UserInfoDto userInfoDto) {
        return userRepository.findByUserName(userInfoDto.getUserName());
    }

    public Boolean signupUser(UserInfoDto userInfoDto) {
        if(Objects.nonNull(checkIfUserAlreadyExist(userInfoDto))){
            return false;
        }
        String encodedPassword = passwordEncoder.encode(userInfoDto.getPassword());
        String userId = UUID.randomUUID().toString();

        UserInfo userInfo = UserInfo.builder()
                .userId(userId)
                .userName(userInfoDto.getUserName())
                .password(encodedPassword)
                .roles(new HashSet<>()).build();
        userRepository.save(userInfo);

        userInfoDto.setUserId(userId);

        // push event into Kafka
        try {
            userInfoProducer.sendEventToKafka(userInfoDto);
        } catch (Exception ex) {
            log.error("Error sending user signup event to Kafka: ", ex);
        }

        return true;
    }
}
