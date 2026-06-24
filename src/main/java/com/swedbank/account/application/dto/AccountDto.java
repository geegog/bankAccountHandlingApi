package com.swedbank.account.application.dto;

import com.swedbank.common.application.Dto.BaseDto;
import com.swedbank.common.application.Dto.MoneyDto;
import com.swedbank.user.application.dto.UserDto;
import lombok.Getter;
import lombok.Setter;

import java.util.Currency;

@Getter
@Setter
public class AccountDto extends BaseDto {

    private String accountNumber;

    private String accountName;

    private MoneyDto balance;

    private UserDto user;

}
