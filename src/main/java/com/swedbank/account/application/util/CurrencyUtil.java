package com.swedbank.account.application.util;

import java.util.Currency;
import java.util.List;

public class CurrencyUtil {

    private static final List<String> SUPPORTED_CURRENCIES = List.of("USD", "EUR", "GBP", "SEK", "NOK", "VND", "JPY", "CHF", "CAD", "AUD");

    public static List<Currency> allowedCurrencies() {
        return Currency.getAvailableCurrencies().stream().filter(
                currency -> SUPPORTED_CURRENCIES.contains(currency.getCurrencyCode())
        ).collect(java.util.stream.Collectors.toList());
    }
}
