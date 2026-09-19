package org.example.controllers;

import org.example.entities.RefreshToken;
import org.example.requests.AuthRequestDTO;
import org.example.requests.RefreshTokenRequestDTO;
import org.example.responses.JWTResponseDTO;
import org.example.services.JWTService;
import org.example.services.RefreshTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TokenController {
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private JWTService jwtService;

    @PostMapping("/auth/v1/login")
    public ResponseEntity<?> AuthenticateAndGetToken(@RequestBody AuthRequestDTO authRequestDTO){
        try {
            if (authRequestDTO == null || authRequestDTO.getUsername() == null || authRequestDTO.getPassword() == null) {
                return new ResponseEntity<>("Username and Password must be provided", HttpStatus.BAD_REQUEST);
            }
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authRequestDTO.getUsername(), authRequestDTO.getPassword())
            );
            if (authentication.isAuthenticated()){
                RefreshToken refreshToken = refreshTokenService.createRefreshToken(authRequestDTO.getUsername());
                return new ResponseEntity<>(JWTResponseDTO.builder()
                        .accessToken(jwtService.generateToken(authRequestDTO.getUsername()))
                        .token(refreshToken.getToken())
                        .build(), HttpStatus.OK);
            }
        } catch (Exception ex) {
            return new ResponseEntity<>("Authentication failed: " + ex.getMessage(), HttpStatus.UNAUTHORIZED);
        }
        return new ResponseEntity<>("Invalid username or password", HttpStatus.UNAUTHORIZED);
    }

    @PostMapping("/auth/v1/refreshToken")
    public JWTResponseDTO refreshToken(@RequestBody RefreshTokenRequestDTO refreshTokenRequestDTO){
        return refreshTokenService.findByToken(refreshTokenRequestDTO.getToken())
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUserInfo)
                .map(userInfo -> {
                    String accessToken = jwtService.generateToken(userInfo.getUserName());
                    return JWTResponseDTO.builder()
                            .accessToken(accessToken)
                            .token(refreshTokenRequestDTO.getToken()).build();
                }).orElseThrow(() -> new RuntimeException("Refresh Token is not in DB..!!"));
    }
}
