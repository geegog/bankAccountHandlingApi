package com.swedbank.account.application.dto;

import com.swedbank.common.application.Dto.MoneyDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccountTransactionRequest {

    @Valid
    private MoneyDto value;

    @NotNull(message = "Account number is required")
    private String accountNumber;

    private String reference;

}
