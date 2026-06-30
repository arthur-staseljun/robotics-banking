package com.robotics.banking.controller;

import com.robotics.banking.dto.request.CreateAccountRequest;
import com.robotics.banking.dto.response.AccountResponse;
import com.robotics.banking.service.AccountService;
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
}
