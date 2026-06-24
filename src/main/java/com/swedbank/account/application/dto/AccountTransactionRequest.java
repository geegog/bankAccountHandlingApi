package com.swedbank.account.application.dto;

import com.swedbank.common.application.Dto.MoneyDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccountTransactionRequest {

    @Valid
    private MoneyDTO value;

    @NotNull(message = "Account number is required")
    private String accountNumber;

}
