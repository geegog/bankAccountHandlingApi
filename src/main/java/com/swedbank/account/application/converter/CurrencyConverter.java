package com.swedbank.account.application.converter;

import com.swedbank.account.application.dto.ConversionResult;
import com.swedbank.common.domian.Money;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;

import static com.swedbank.account.application.util.CurrencyUtil.RATES_TO_EUR;

@Component
public class CurrencyConverter {

    public ConversionResult convert(Currency fromCurrency, Currency toCurrency, BigDecimal amount) {
        Money originalMoney = Money.of(amount, fromCurrency);

        if (fromCurrency.equals(toCurrency)) {
            return new ConversionResult(originalMoney, originalMoney, BigDecimal.ONE);
        }

        BigDecimal rateToEur = RATES_TO_EUR.get(fromCurrency.getCurrencyCode().toUpperCase());
        if (rateToEur == null) throw new IllegalArgumentException("Unsupported source currency: " + fromCurrency);

        BigDecimal amountInEur = amount.divide(rateToEur, 6, RoundingMode.HALF_UP);

        BigDecimal rateFromEur = RATES_TO_EUR.get(toCurrency.getCurrencyCode().toUpperCase());
        if (rateFromEur == null) throw new IllegalArgumentException("Unsupported target currency: " + toCurrency);

        int scale = toCurrency == Currency.getInstance("VND") ? 0 : 2;
        BigDecimal convertedValue = amountInEur.multiply(rateFromEur).setScale(scale, RoundingMode.HALF_UP);
        Money convertedMoney = Money.of(convertedValue, Currency.getInstance(toCurrency.getCurrencyCode().toUpperCase()));

        BigDecimal effectiveRate = convertedValue.divide(amount, 6, RoundingMode.HALF_UP);

        return new ConversionResult(originalMoney, convertedMoney, effectiveRate);
    }

}
