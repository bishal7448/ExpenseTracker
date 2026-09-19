package org.example.services;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.example.entities.UserInfo;
import org.example.models.UserInfoDto;
import org.example.repositories.UserRepository;
import org.jspecify.annotations.NullMarked;
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
public class UserDetailsServiceImpl implements UserDetailsService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

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
        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(userId);
        userInfo.setUserName(userInfoDto.getUserName());
        userInfo.setPassword(encodedPassword);
        userInfo.setRoles(new HashSet<>());
        userRepository.save(userInfo);
        return true;
    }
}
