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
        if (transactionRequest.getReference() != null) {
            transaction.setReference(transactionRequest.getReference());
        }
        transaction.setValue(Money.of(transactionRequest.getValue().getAmount(), transactionRequest.getValue().getCurrency()));
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
