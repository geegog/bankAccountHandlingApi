package com.swedbank.transaction.application.service;

import com.swedbank.common.domian.Money;
import com.swedbank.transaction.application.dto.PagedResult;
import com.swedbank.transaction.application.dto.TransactionDto;
import com.swedbank.transaction.application.dto.TransactionRequest;
import com.swedbank.transaction.application.dto.TransactionSearch;
import com.swedbank.transaction.domian.model.Transaction;
import com.swedbank.transaction.domian.repository.TransactionRepository;
import com.swedbank.user.application.service.UserService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;

    private final ModelMapper modelMapper;
    private final UserService userService;

    private void saveTransaction(Transaction transaction) {
        transactionRepository.save(transaction);
    }

    public void recordTransaction(TransactionRequest transactionRequest) {
        Transaction transaction = new Transaction();
        transaction.setTransactionType(transactionRequest.getTransactionType());
        transaction.setUserId(transactionRequest.getUserId());
        transaction.setAccountNumber(transactionRequest.getAccountNumber());
        transaction.setBalance(Money.of(transactionRequest.getBalance().getAmount(),
                transactionRequest.getBalance().getCurrency()));
        if (transactionRequest.getReference() != null) {
            transaction.setReference(transactionRequest.getReference());
        }
        if (transactionRequest.getTargetValue() != null) {
            transaction.setTargetValue(Money.of(transactionRequest.getTargetValue().getAmount(),
                    transactionRequest.getTargetValue().getCurrency()));
        }
        if (transactionRequest.getTargetAccountNumber() != null) {
            transaction.setTargetAccountNumber(transactionRequest.getTargetAccountNumber());
        }
        if (transactionRequest.getExchangeRate() != null) {
            transaction.setExchangeRate(transactionRequest.getExchangeRate());
        }
        if (transactionRequest.getTargetBalance()  != null) {
            transaction.setTargetBalance(Money.of(transactionRequest.getTargetBalance().getAmount(),
                    transactionRequest.getTargetBalance().getCurrency()));
        }
        transaction.setValue(Money.of(transactionRequest.getValue().getAmount(),
                transactionRequest.getValue().getCurrency()));
        saveTransaction(transaction);
    }

    public PagedResult<TransactionDto> getTransactions(TransactionSearch transactionSearch, String accountNumber, String email) {

        var user = userService.getUserByEmail(email);

        Pageable pageable = PageRequest.of(transactionSearch.getPage(), transactionSearch.getSize());

        var transactionPage = transactionRepository.findByAccountNumberAndUserId(accountNumber, user.getId(), pageable);

        return modelMapper.map(
                transactionPage,
                new TypeToken<PagedResult<TransactionDto>>() {}.getType()
        );

    }

}
