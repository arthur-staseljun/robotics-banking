package com.robotics.banking.service;

import com.robotics.banking.dto.response.TransactionResponse;
import com.robotics.banking.entity.Transaction;
import com.robotics.banking.persistence.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountService accountService;

    @Transactional(readOnly = true)
    public TransactionResponse get(Long id) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Transaction " + id + " not found"));
        return toResponse(transaction);
    }

    @Transactional(readOnly = true)
    public Page<TransactionResponse> getHistory(Long accountId, Pageable pageable) {
        accountService.getEntity(accountId);
        return transactionRepository.findByAccountId(accountId, pageable)
                .map(this::toResponse);
    }

    private TransactionResponse toResponse(Transaction transaction) {
        return new TransactionResponse(
                transaction.getId(),
                transaction.getAccount().getId(),
                transaction.getType(),
                transaction.getAmount(),
                transaction.getCurrency(),
                transaction.getBalanceAfter(),
                transaction.getDescription(),
                transaction.getCreatedAt(),
                transaction.getCounterpartyTransactionId());
    }
}
