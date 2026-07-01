package com.robotics.banking.controller;

import com.robotics.banking.dto.response.TransactionResponse;
import com.robotics.banking.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @GetMapping("/accounts/{accountId}/transactions")
    public Page<TransactionResponse> history(
            @PathVariable Long accountId,
            @PageableDefault(size = 10) Pageable pageable) {
        Pageable newestFirst = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                Sort.by(Sort.Direction.DESC, "createdAt"));
        return transactionService.getHistory(accountId, newestFirst);
    }

    @GetMapping("/transactions/{id}")
    public TransactionResponse get(@PathVariable Long id) {
        return transactionService.get(id);
    }
}
