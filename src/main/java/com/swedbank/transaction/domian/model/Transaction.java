package com.swedbank.transaction.domian.model;

import com.swedbank.common.domian.EntityBase;
import com.swedbank.common.domian.Money;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
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
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "source_amount", precision = 19, scale = 2)),
            @AttributeOverride(name = "currency", column = @Column(name = "source_currency", length = 3))
    })
    private Money value;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "target_amount", precision = 19, scale = 2)),
            @AttributeOverride(name = "currency", column = @Column(name = "target_currency", length = 3))
    })
    private Money targetValue;

    @Enumerated(EnumType.STRING)
    private TransactionType transactionType;

    private String reference;

    private BigDecimal exchangeRate;

}
