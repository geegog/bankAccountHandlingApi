package com.swedbank.account.domain.repository;

import com.swedbank.account.domain.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public interface AccountRepository extends JpaRepository<Account, UUID> {

    Optional<Account> findByAccountNumberAndUser_Email(String accountNumber, String userEmail);

    Set<Account> findByUser_Email(String userEmail);

}
