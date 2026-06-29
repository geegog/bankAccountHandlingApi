package com.swedbank.account.application.infrastructure.bootstrap;

import com.swedbank.account.application.dto.AccountDto;
import com.swedbank.account.application.dto.AccountTransactionRequest;
import com.swedbank.account.application.dto.ExchangeRequest;
import com.swedbank.account.domain.model.CreateAccountRequest;
import com.swedbank.account.rest.AccountController;
import com.swedbank.auth.application.util.JwtUtil;
import com.swedbank.common.application.dto.MoneyDto;
import com.swedbank.user.application.dto.UserAccountRequest;
import com.swedbank.user.application.dto.UserDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;

@Slf4j
@Component
@Profile({"local", "dev"})
@RequiredArgsConstructor
public class DemoDataSeeder {

    private final AccountController accountController;

    @Value("${security.jwt.secret}")
    private String secret;

    @Value("${security.jwt.expiration}")
    private int expiration;

    @EventListener(ApplicationReadyEvent.class)
    public void seedDemoData() {
        log.info("🚀 Starting local development database API seeding sequence...");

        try {
            UserAccountRequest seedRequest = new UserAccountRequest();
            seedRequest.setUser(buildTestUser());
            seedRequest.setCreateAccounts(List.of(
                    buildAccountPayload("Checking USD", "USD"),
                    buildAccountPayload("Savings EUR", "EUR"),
                    buildAccountPayload("Savings SEK", "SEK"),
                    buildAccountPayload("Savings VND", "VND")
            ));

            var responseEntity = accountController.createUserAccount(seedRequest);
            List<AccountDto> createdAccounts = responseEntity.getBody();

            if (createdAccounts != null && !createdAccounts.isEmpty()) {
                log.info("✅ Successfully bootstrapped {} base checking accounts via API layer.", createdAccounts.size());

                String primaryAccountNumber = createdAccounts.get(0).getAccountNumber();
                String secondaryAccountNumber = createdAccounts.get(1).getAccountNumber();
                String sekAccountNumber = createdAccounts.get(2).getAccountNumber();
                String vndAccountNumber = createdAccounts.get(3).getAccountNumber();
                executeInitialDeposit(primaryAccountNumber, "USD", "2750");
                executeInitialDeposit(secondaryAccountNumber, "EUR", "250");
                executeInitialDeposit(sekAccountNumber, "SEK", "35000.34");
                executeInitialDeposit(vndAccountNumber, "VND", "5000000000");
                executeWithdrawals(primaryAccountNumber);
                executeExchange(primaryAccountNumber, secondaryAccountNumber);
            }

            log.info("✅ JWT Token for FE use: {}", getToken());

        } catch (Exception e) {
            log.error("❌ Critical failure during startup API data seeding: ", e);
        }
    }

    private User getAuthenticatedUser() {
        var user = buildTestUser();
        return new User(user.getEmail(), "", List.of());
    }

    private UserDto buildTestUser() {
        UserDto user = new UserDto();
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setEmail("john.doe@swedbank.local");
        user.setPassword("internal_secure_pass");
        return user;
    }

    private CreateAccountRequest buildAccountPayload(String name, String currencyCode) {
        CreateAccountRequest request = new CreateAccountRequest();
        request.setAccountName(name);
        request.setCurrency(Currency.getInstance(currencyCode));
        return request;
    }

    private void executeInitialDeposit(String accountNumber, String currencyCode, String amount) {
        AccountTransactionRequest deposit = new AccountTransactionRequest();
        deposit.setAccountNumber(accountNumber);
        deposit.setValue(MoneyDto.builder()
                .amount(new BigDecimal(amount))
                .currency(Currency.getInstance(currencyCode))
                .build());
        deposit.setReference("Money to spend on everything");

        accountController.addMoneyToAccount(deposit, getAuthenticatedUser());
        log.info("💰 Successfully deposited startup credit of {} {} to account: {}", amount, currencyCode, accountNumber);
    }

    private void executeWithdrawals(String accountNumber) {
        for (int i = 1; i <= 60; i++) {
            var val = i * 1.34;
            AccountTransactionRequest withdrawal = new AccountTransactionRequest();
            withdrawal.setAccountNumber(accountNumber);
            withdrawal.setValue(MoneyDto.builder()
                    .amount(new BigDecimal(val))
                    .currency(Currency.getInstance("USD"))
                    .build());
            accountController.removeMoneyFromAccount(withdrawal, getAuthenticatedUser());
        }
        log.info("💰 Withdrawal from account: {} where successful", accountNumber);

    }

    private void executeExchange(String accountNumber, String destinationAccountNumber) {
        ExchangeRequest  exchange = new ExchangeRequest();
        exchange.setSourceAccountNumber(accountNumber);
        exchange.setDestinationAccountNumber(destinationAccountNumber);
        exchange.setValue(MoneyDto.builder()
                .amount(new BigDecimal("25.00"))
                .currency(Currency.getInstance("USD"))
                .build());

        accountController.moveMoneyBetweenAccounts(exchange, getAuthenticatedUser());

        log.info("💰 Exchange between account {} and {} was successful", accountNumber, destinationAccountNumber);
    }

    private String getToken() {
        return JwtUtil.generateToken(buildTestUser(), secret, expiration);
    }
}
