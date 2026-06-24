package com.swedbank.auth.application.service;


import com.swedbank.auth.application.config.JwtConfigParams;
import com.swedbank.auth.application.dto.AuthRequest;
import com.swedbank.auth.application.dto.AuthResult;
import com.swedbank.auth.application.util.JwtUtil;
import com.swedbank.user.application.dto.UserDto;
import com.swedbank.user.application.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthenticationService {

    private final JwtConfigParams jwtConfigParams;

    private final UserService userService;

    @Value("${security.jwt.expiration}")
    private Long expirationMs;

    public AuthResult authenticate(AuthRequest request) {
        UserDto userDto;
        try {
            userDto = userService.verifyUser(request.username(), request.password());
        } catch (Exception e) {
            throw new RuntimeException("Invalid credentials");
        }
        String accessToken = JwtUtil.generateToken(
            userDto,
            jwtConfigParams.getSecret(),
            jwtConfigParams.getExpiration()
        );
        return new AuthResult(
            userDto,
            accessToken,
            new Date(System.currentTimeMillis() + expirationMs)
        );
    }

}
