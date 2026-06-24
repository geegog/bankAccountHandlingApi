package com.swedbank.account.rest;

import com.swedbank.account.application.dto.AccountDto;
import com.swedbank.account.application.dto.AccountTransactionRequest;
import com.swedbank.account.application.dto.ExchangeRequest;
import com.swedbank.account.application.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "${api.url.prefix}${api.version}/account")
public class AccountController {

    private final AccountService accountService;

    @PostMapping(value = "/deposit")
    public ResponseEntity<AccountDto> addMoneyToAccount(
            @RequestBody @Valid AccountTransactionRequest accountTransactionRequest,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok().body(
                accountService.depositMoney(accountTransactionRequest, user.getUsername()));
    }

    @PostMapping(value = "/withdraw")
    public ResponseEntity<AccountDto> removeMoneyFromAccount(
            @RequestBody @Valid AccountTransactionRequest accountTransactionRequest,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok().body(
                accountService.withdrawMoney(accountTransactionRequest, user.getUsername()));
    }

    @PostMapping(value = "/exchange")
    public ResponseEntity<?> moveMoneyBetweenAccounts(
            @RequestBody @Valid ExchangeRequest exchangeRequest,
            @AuthenticationPrincipal User user
    ) {
        accountService.currencyExchange(exchangeRequest, user.getUsername());
        return ResponseEntity.noContent().build();
    }

    @GetMapping(value = "/{accountNumber}")
    public ResponseEntity<AccountDto> getAccountBalance(
            @AuthenticationPrincipal User user,
            @PathVariable String accountNumber) {
        return ResponseEntity.ok(accountService.getAccount(accountNumber, user.getUsername()));
    }

    @GetMapping(value = "/all")
    public ResponseEntity<Set<AccountDto>> getAllAccounts(
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(accountService.getAccountsByUserEmail(user.getUsername()));
    }

}
