package com.swedbank.account.application.infrastructure.bootstrap;

import com.swedbank.account.application.dto.AccountDto;
import com.swedbank.account.application.dto.AccountTransactionRequest;
import com.swedbank.account.domain.model.CreateAccountRequest;
import com.swedbank.account.rest.AccountController;
import com.swedbank.auth.application.util.JwtUtil;
import com.swedbank.common.application.Dto.MoneyDto;
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
                    buildAccountPayload("Savings EUR", "EUR")
            ));

            var responseEntity = accountController.createUserAccount(seedRequest);
            List<AccountDto> createdAccounts = responseEntity.getBody();

            if (createdAccounts != null && !createdAccounts.isEmpty()) {
                log.info("✅ Successfully bootstrapped {} base checking accounts via API layer.", createdAccounts.size());

                String primaryAccountNumber = createdAccounts.get(0).getAccountNumber();
                executeInitialDeposit(primaryAccountNumber);
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

    private void executeInitialDeposit(String accountNumber) {
        AccountTransactionRequest deposit = new AccountTransactionRequest();
        deposit.setAccountNumber(accountNumber);
        deposit.setValue(MoneyDto.builder()
                .amount(new BigDecimal("2500.00"))
                .currency(Currency.getInstance("USD"))
                .build());
        deposit.setReference("TEST_USER_STARTUP_CREDIT");

        accountController.addMoneyToAccount(deposit, getAuthenticatedUser());
        log.info("💰 Successfully deposited startup credit of $2500.00 to account: {}", accountNumber);
    }

    private String getToken() {
        return JwtUtil.generateToken(buildTestUser(), secret, expiration);
    }
}
