package com.swedbank.account.domian.model;

import com.swedbank.common.domian.EntityBase;
import com.swedbank.common.domian.Money;
import com.swedbank.user.domain.model.User;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Currency;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "accounts")
public class Account extends EntityBase {

    private String accountNumber;

    private String accountName;

    private Currency currency;

    @Embedded
    private Money balance;

    @ManyToOne
    private User user;
}
