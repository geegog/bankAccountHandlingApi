package com.swedbank.account.application.validation;

import com.swedbank.account.application.annotation.AllowedCurrency;
import com.swedbank.account.application.util.CurrencyUtil;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Currency;

public class CurrencyValidator implements ConstraintValidator<AllowedCurrency, Currency> {

    @Override
    public void initialize(AllowedCurrency constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(Currency value, ConstraintValidatorContext context) {
        return CurrencyUtil.allowedCurrencies().contains(value);
    }
}
