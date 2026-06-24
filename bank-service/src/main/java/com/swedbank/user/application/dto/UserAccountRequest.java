package com.swedbank.user.application.dto;

import com.swedbank.account.domian.model.CreateAccountRequest;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserAccountRequest {

    @Valid
    private UserDto user;

    @Valid
    private CreateAccountRequest createAccount;

}
