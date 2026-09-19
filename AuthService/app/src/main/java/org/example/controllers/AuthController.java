package org.example.controllers;

import lombok.AllArgsConstructor;
import org.example.entities.RefreshToken;
import org.example.models.UserInfoDto;
import org.example.responses.JWTResponseDTO;
import org.example.services.JWTService;
import org.example.services.RefreshTokenService;
import org.example.services.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@AllArgsConstructor
@RestController
public class AuthController {
    @Autowired
    private JWTService jwtService;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @PostMapping("/auth/v1/signup")
    public ResponseEntity SignUp(@RequestBody UserInfoDto userInfoDto) {
        try{
            Boolean isSignUped = userDetailsService.signupUser(userInfoDto);
            if(Boolean.FALSE.equals(isSignUped)){
                return new ResponseEntity<>("Already Exist", HttpStatus.BAD_REQUEST);
            }
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(userInfoDto.getUserName());
            String jwtToken = jwtService.generateToken(userInfoDto.getUserName());
            return new ResponseEntity<>(JWTResponseDTO.builder().accessToken(jwtToken).
                    token(refreshToken.getToken()).build(), HttpStatus.OK);
        }catch(Exception ex) {
            return new ResponseEntity<>("Exception in User Service", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
