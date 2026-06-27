package com.swedbank.account.application.service;

import com.swedbank.account.application.converter.CurrencyConverter;
import com.swedbank.account.application.dto.AccountDto;
import com.swedbank.account.application.dto.AccountTransactionRequest;
import com.swedbank.account.application.dto.ExchangeRequest;
import com.swedbank.account.application.dto.ExchangeResponse;
import com.swedbank.account.application.infrastructure.aop.SimulateExternalLog;
import com.swedbank.account.application.util.AccountNumberGenerator;
import com.swedbank.account.domain.model.Account;
import com.swedbank.account.domain.model.CreateAccountRequest;
import com.swedbank.account.domain.repository.AccountRepository;
import com.swedbank.common.application.Dto.MoneyDto;
import com.swedbank.common.application.exception.InsufficientException;
import com.swedbank.common.application.exception.MismatchException;
import com.swedbank.common.application.exception.NotFoundException;
import com.swedbank.common.domian.Money;
import com.swedbank.transaction.application.dto.TransactionRequest;
import com.swedbank.transaction.application.service.TransactionService;
import com.swedbank.transaction.domian.model.TransactionType;
import com.swedbank.user.application.dto.UserAccountRequest;
import com.swedbank.user.application.dto.UserDto;
import com.swedbank.user.application.service.UserService;
import com.swedbank.user.domain.model.User;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final TransactionService transactionService;

    private final AccountRepository accountRepository;

    private final CurrencyConverter currencyConverter;

    private final UserService userService;

    private final ModelMapper modelMapper;

    private Account getAccountByNumberAndUser(String accountNumber, String userEmail) {
        return accountRepository.findByAccountNumberAndUser_Email(accountNumber, userEmail)
                .orElseThrow(() -> new NotFoundException("Account not found for user"));
    }

    private Set<Account> getAccountsByUser(String userEmail) {
        return accountRepository.findByUser_Email(userEmail);
    }

    @Transactional
    public AccountDto depositMoney(AccountTransactionRequest accountTransactionRequest, String email) {

        var user = userService.getUserByEmail(email);

        var account = getAccountByNumberAndUser(accountTransactionRequest.getAccountNumber(), user.getEmail());

        validateCurrency(account.getBalance().getCurrency(), accountTransactionRequest.getValue().getCurrency());

        var newBalance = account.getBalance().getAmount() != null ? account.getBalance().getAmount().add(accountTransactionRequest.getValue().getAmount()) : null;
        account.setBalance(Money.of(newBalance, account.getBalance().getCurrency()));

        var updateAccount = accountRepository.save(account);

        logTransaction(accountTransactionRequest, user, TransactionType.CREDIT);

        return modelMapper.map(updateAccount, AccountDto.class);

    }

    private void logTransaction(AccountTransactionRequest accountTransactionRequest, UserDto user, TransactionType transactionType) {
        TransactionRequest transactionRequest = new TransactionRequest();
        transactionRequest.setAccountNumber(accountTransactionRequest.getAccountNumber());
        transactionRequest.setUserId(user.getId());
        transactionRequest.setReference(accountTransactionRequest.getReference());
        transactionRequest.setValue(accountTransactionRequest.getValue());
        transactionRequest.setTransactionType(transactionType);

        transactionService.recordTransaction(transactionRequest);
    }

    private void logTransaction(TransactionRequest transactionRequest) {
        transactionService.recordTransaction(transactionRequest);
    }

    private static void validateCurrency(Currency accountCurrency, Currency suppliedCurrency) {

        if ( suppliedCurrency != accountCurrency) {
            throw new MismatchException("Transaction currency must match the account currency " + accountCurrency);
        }
    }

    private static void validateBalanceAmount(BigDecimal accountBalanceValue, BigDecimal transactionValue) {
        if (accountBalanceValue == null || accountBalanceValue.compareTo(transactionValue) < 0) {
            throw new InsufficientException("Insufficient balance");
        }
    }

    @SimulateExternalLog
    @Transactional
    public AccountDto withdrawMoney(AccountTransactionRequest accountTransactionRequest, String email) {

        var user = userService.getUserByEmail(email);

        var account = getAccountByNumberAndUser(accountTransactionRequest.getAccountNumber(), user.getEmail());

        validateCurrency(account.getBalance().getCurrency(), accountTransactionRequest.getValue().getCurrency());
        validateBalanceAmount(account.getBalance().getAmount(), accountTransactionRequest.getValue().getAmount());

        var newBalance = account.getBalance().getAmount().subtract(accountTransactionRequest.getValue().getAmount());
        account.setBalance(Money.of(newBalance, account.getBalance().getCurrency()));

        var updatedAccount = accountRepository.save(account);

        logTransaction(accountTransactionRequest, user, TransactionType.DEBIT);

        return modelMapper.map(updatedAccount, AccountDto.class);

    }

    public AccountDto getAccount(String accountNumber, String email) {

        var user = userService.getUserByEmail(email);

        var account = getAccountByNumberAndUser(accountNumber, user.getEmail());

        return modelMapper.map(account, AccountDto.class);

    }

    public Set<AccountDto> getAccountsByUserEmail(String email) {
        var user = userService.getUserByEmail(email);

        var accounts = getAccountsByUser(user.getEmail());

        return accounts.stream()
                .map(account -> modelMapper.map(account, AccountDto.class))
                .collect(java.util.stream.Collectors.toSet());
    }

    @Transactional
    public ExchangeResponse currencyExchange(ExchangeRequest exchangeRequest, String email) {

        var sourceAccount = getAccountByNumberAndUser(exchangeRequest.getSourceAccountNumber(), email);
        validateCurrency(sourceAccount.getBalance().getCurrency(), exchangeRequest.getValue().getCurrency());
        validateBalanceAmount(sourceAccount.getBalance().getAmount(), exchangeRequest.getValue().getAmount());

        // debit source
        AccountTransactionRequest sourceRequest = new AccountTransactionRequest();
        sourceRequest.setAccountNumber(sourceAccount.getAccountNumber());
        sourceRequest.setValue(exchangeRequest.getValue());

        var sourceAfterWithdrawal = withdrawMoney(sourceRequest, email);

        var destinationAccount = getAccountByNumberAndUser(exchangeRequest.getDestinationAccountNumber(), email);
        var valueAfterConversion = currencyConverter.convert(sourceAccount.getBalance().getCurrency(), destinationAccount.getBalance().getCurrency(), exchangeRequest.getValue().getAmount());


        // credit source
        var targetValue = MoneyDto.toDto(valueAfterConversion.converted());
        AccountTransactionRequest destinationRequest = new AccountTransactionRequest();
        destinationRequest.setAccountNumber(exchangeRequest.getDestinationAccountNumber());
        destinationRequest.setValue(targetValue);
        var targetAfterDeposit = depositMoney(destinationRequest, email);

        TransactionRequest transactionRequest = new TransactionRequest();
        transactionRequest.setAccountNumber(exchangeRequest.getSourceAccountNumber());
        transactionRequest.setUserId(userService.getUserByEmail(email).getId());
        transactionRequest.setValue(exchangeRequest.getValue());
        transactionRequest.setTransactionType(TransactionType.EXCHANGE);
        transactionRequest.setTargetValue(targetValue);
        transactionRequest.setExchangeRate(valueAfterConversion.effectiveRate());

        logTransaction(transactionRequest);

        return ExchangeResponse.builder()
                .destinationAccount(targetAfterDeposit)
                .sourceAccount(sourceAfterWithdrawal)
                .conversionResult(valueAfterConversion)
                .build();

    }

    public List<AccountDto> createAccount(List<CreateAccountRequest> accountRequests, User user) {
        List<Account> accounts = accountRequests.stream().map(accountRequest -> {
            Account account = new Account();
            account.setAccountName(accountRequest.getAccountName());
            account.setBalance(Money.of(BigDecimal.ZERO, accountRequest.getCurrency()));
            account.setAccountNumber(AccountNumberGenerator.generateAccountNumber());
            account.setUser(user);
            return account;
        }).collect(Collectors.toList());

        return modelMapper.map( accountRepository.saveAll(accounts), new TypeToken<List<AccountDto>>() {}.getType());
    }

    @Transactional
    public List<AccountDto> createUserAccount(UserAccountRequest userAccountRequest) {
        var user = userService.createUser(userAccountRequest.getUser());

        return createAccount(userAccountRequest.getCreateAccounts(), user);
    }

}
