package com.swedbank.account.application.dto;

import com.swedbank.common.domian.Money;

import java.math.BigDecimal;

public record ConversionResult(
        Money original,
        Money converted,
        BigDecimal effectiveRate
) {}
