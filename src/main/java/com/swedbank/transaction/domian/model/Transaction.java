package com.swedbank.transaction.domian.model;

import com.swedbank.common.domian.EntityBase;
import com.swedbank.common.domian.Money;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "transactions")
public class Transaction extends EntityBase {

    private String accountNumber;

    private UUID userId;

    @Embedded
    private Money value;

    @Embedded
    private Money targetValue;

    @Enumerated(EnumType.STRING)
    private TransactionType transactionType;

    private String reference;

    private BigDecimal exchangeRate;

}
