package com.swedbank.user.application.dto;

import com.swedbank.account.domain.model.CreateAccountRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UserAccountRequest {

    @Valid
    @NotNull(message = "User cannot be null")
    private UserDto user;

    @Valid
    @NotNull(message = "Create accounts cannot be null")
    @Size(min = 1, max = 4, message = "The request must contain between 1 and 4 account applications.")
    private List<CreateAccountRequest> createAccounts;

}
