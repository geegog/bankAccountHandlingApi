package com.swedbank.transaction.application.dto;

import com.swedbank.common.application.Dto.BaseDto;
import com.swedbank.common.application.Dto.MoneyDto;
import com.swedbank.transaction.domian.model.TransactionType;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
public class TransactionDto extends BaseDto {

    private String accountNumber;

    private String targetAccountNumber;

    private UUID userId;

    private MoneyDto value;

    private MoneyDto targetValue;

    private MoneyDto balance;

    private MoneyDto targetBalance;

    private TransactionType transactionType;

    private String reference;

    private BigDecimal exchangeRate;


}
