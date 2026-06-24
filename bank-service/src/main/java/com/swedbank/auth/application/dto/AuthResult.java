package com.swedbank.auth.application.dto;

import com.swedbank.user.application.dto.UserDto;

import java.util.Date;

public record AuthResult(UserDto userDto, String accessToken, Date expiration) {}
