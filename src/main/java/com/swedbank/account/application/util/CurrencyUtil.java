package com.swedbank.account.application.util;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;
import java.util.Map;

public class CurrencyUtil {

    private static final List<String> SUPPORTED_CURRENCIES = List.of("USD", "EUR", "GBP", "SEK", "VND");

    public static final Map<String, BigDecimal> RATES_TO_EUR = Map.of(
            "EUR", BigDecimal.ONE,
            "USD", new BigDecimal("1.10"),
            "GBP", new BigDecimal("0.85"),
            "SEK", new BigDecimal("11.50"),
            "VND", new BigDecimal("27000.00")
    );

    public static List<Currency> allowedCurrencies() {
        return Currency.getAvailableCurrencies().stream().filter(
                currency -> SUPPORTED_CURRENCIES.contains(currency.getCurrencyCode())
        ).collect(java.util.stream.Collectors.toList());
    }

}
