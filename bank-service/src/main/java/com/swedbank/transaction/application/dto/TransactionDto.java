package com.swedbank.transaction.application.dto;

import com.swedbank.account.application.dto.AccountDto;
import com.swedbank.common.application.Dto.BaseDto;
import com.swedbank.common.application.Dto.MoneyDto;
import com.swedbank.transaction.domian.model.TransactionType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TransactionDto extends BaseDto {

    private AccountDto account;

    private MoneyDto value;

    private TransactionType transactionType;

    private String reference;


}
