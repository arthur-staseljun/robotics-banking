package com.robotics.banking.controller;

import com.robotics.banking.dto.request.CreateAccountRequest;
import com.robotics.banking.dto.request.DebitRequest;
import com.robotics.banking.dto.request.DepositRequest;
import com.robotics.banking.dto.request.ExchangeRequest;
import com.robotics.banking.dto.response.AccountResponse;
import com.robotics.banking.dto.response.ExchangeResponse;
import com.robotics.banking.dto.response.TransactionResponse;
import com.robotics.banking.service.AccountService;
import com.robotics.banking.service.DebitService;
import com.robotics.banking.service.ExchangeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;
    private final DebitService debitService;
    private final ExchangeService exchangeService;

    @PostMapping("/users/{userId}/accounts")
    @ResponseStatus(HttpStatus.CREATED)
    public AccountResponse create(@PathVariable Long userId, @Valid @RequestBody CreateAccountRequest request) {
        return accountService.create(userId, request);
    }

    @GetMapping("/users/{userId}/accounts")
    public List<AccountResponse> listByUser(@PathVariable Long userId) {
        return accountService.listByUser(userId);
    }

    @GetMapping("/accounts/{id}")
    public AccountResponse get(@PathVariable Long id) {
        return accountService.get(id);
    }

    @PostMapping("/accounts/{id}/deposit")
    public TransactionResponse deposit(@PathVariable Long id, @Valid @RequestBody DepositRequest request) {
        return accountService.deposit(id, request);
    }

    @PostMapping("/accounts/{id}/debit")
    public TransactionResponse debit(@PathVariable Long id,
                                     @Valid @RequestBody DebitRequest request) {
        return debitService.debit(id, request);
    }

    @PostMapping("/accounts/exchange")
    public ExchangeResponse exchange(@Valid @RequestBody ExchangeRequest request) {
        return exchangeService.exchange(request);
    }
}
