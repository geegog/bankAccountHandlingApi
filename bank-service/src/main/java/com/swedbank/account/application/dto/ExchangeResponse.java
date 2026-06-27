package com.swedbank.account.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExchangeResponse {

    private AccountDto sourceAccount;

    private AccountDto destinationAccount;

    private ConversionResult conversionResult;

}
