package com.robotics.banking.service;

import com.robotics.banking.dto.request.DebitRequest;
import com.robotics.banking.dto.response.TransactionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DebitService {

    private final ExternalLogService externalLogService;
    private final AccountService accountService;

    public TransactionResponse debit(Long accountId, DebitRequest request) {
        externalLogService.logBeforeDebit(accountId);
        return accountService.debit(accountId, request);
    }
}
