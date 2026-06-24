package com.swedbank.account.application.service;

import com.swedbank.account.application.dto.AccountDto;
import com.swedbank.account.application.dto.AccountTransactionRequest;
import com.swedbank.account.application.util.AccountNumberGenerator;
import com.swedbank.account.domian.model.Account;
import com.swedbank.account.domian.model.CreateAccountRequest;
import com.swedbank.account.domian.repository.AccountRepository;
import com.swedbank.common.domian.Money;
import com.swedbank.user.application.service.UserService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;

    private final UserService userService;

    private final ModelMapper modelMapper;

    private Account getAccountByNumberAndUser(String accountNumber, String userEmail) {
        return accountRepository.finByAccountNumberAndUser_Email(accountNumber, userEmail)
                .orElseThrow(() -> new RuntimeException("Account not found for user"));
    }

    private Set<Account> getAccountsByUser(String userEmail) {
        return accountRepository.findByUser_Email(userEmail);
    }

    public void depositMoney(AccountTransactionRequest accountTransactionRequest, String email) {

        var user = userService.getUserByEmail(email);

        var account = getAccountByNumberAndUser(accountTransactionRequest.getAccountNumber(), user.getEmail());

        var newBalance = account.getBalance().getAmount() != null ? account.getBalance().getAmount().add(accountTransactionRequest.getValue().getAmount()) : null;
        account.setBalance(Money.of(newBalance, account.getBalance().getCurrency()));

        accountRepository.save(account);

    }

    public void withdrawMoney(AccountTransactionRequest accountTransactionRequest, String email) {

        var user = userService.getUserByEmail(email);

        var account = getAccountByNumberAndUser(accountTransactionRequest.getAccountNumber(), user.getEmail());

        if (account.getBalance().getAmount() == null || account.getBalance().getAmount().compareTo(accountTransactionRequest.getValue().getAmount()) < 0) {
            throw new RuntimeException("Insufficient balance");
        }

        var newBalance = account.getBalance().getAmount().subtract(accountTransactionRequest.getValue().getAmount());
        account.setBalance(Money.of(newBalance, account.getBalance().getCurrency()));

        accountRepository.save(account);

    }

    public AccountDto getAccountBalance(AccountTransactionRequest accountTransactionRequest, String email) {

        var user = userService.getUserByEmail(email);

        var account = getAccountByNumberAndUser(accountTransactionRequest.getAccountNumber(), user.getEmail());

        return modelMapper.map(account, AccountDto.class);

    }

    public Set<AccountDto> getAccountsByUserEmail(String email) {
        var user = userService.getUserByEmail(email);

        var accounts = getAccountsByUser(user.getEmail());

        return accounts.stream()
                .map(account -> modelMapper.map(account, AccountDto.class))
                .collect(java.util.stream.Collectors.toSet());
    }

    public AccountDto createAccount(CreateAccountRequest accountRequest) {
        Account account = new Account();
        account.setAccountName(accountRequest.getAccountName());
        account.setBalance(Money.of(BigDecimal.ZERO, accountRequest.getCurrency()));
        account.setAccountNumber(AccountNumberGenerator.generateFormattedNumber());
        accountRepository.save(account);
        return modelMapper.map(account, AccountDto.class);
    }

}
