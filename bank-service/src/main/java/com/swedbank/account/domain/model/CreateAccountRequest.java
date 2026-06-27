package com.swedbank.account.domain.model;

import com.swedbank.account.application.annotation.AllowedCurrency;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.Currency;

@Getter
@Setter
public class CreateAccountRequest {

    @NotNull(message = "Account number is required")
    @Size(max = 50, message = "Account name cannot be more than 150 characters")
    private String accountName;

    @NotNull(message = "Currency is required")
    @AllowedCurrency
    private Currency currency;

}
