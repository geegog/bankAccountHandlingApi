package com.swedbank.account.application.dto;

import com.swedbank.common.application.Dto.MoneyDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExchangeRequest {

    @NotNull(message = "Source Account Number cannot be null")
    private String sourceAccountNumber;

    @NotNull(message = "Destination Account Number cannot be null")

    private String destinationAccountNumber;

    @Valid
    private MoneyDto value;

}
