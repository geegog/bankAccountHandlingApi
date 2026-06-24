package com.swedbank.account.domian.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Currency;

@Getter
@Setter
public class CreateAccountRequest {

    private String accountName;

    private Currency currency;

}
