package com.robotics.banking.service;

import com.robotics.banking.dto.request.CreateAccountRequest;
import com.robotics.banking.dto.response.AccountResponse;
import com.robotics.banking.model.Account;
import com.robotics.banking.model.User;
import com.robotics.banking.persistence.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final UserService userService;

    @Transactional
    public AccountResponse create(Long userId, CreateAccountRequest request) {
        User user = userService.getEntity(userId);
        Account account = new Account(user, request.currency());
        return toResponse(accountRepository.save(account));
    }

    @Transactional(readOnly = true)
    public AccountResponse get(Long id) {
        return toResponse(getEntity(id));
    }

    @Transactional(readOnly = true)
    public List<AccountResponse> listByUser(Long userId) {
        return accountRepository.findByUserId(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    Account getEntity(Long id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account " + id + " not found"));
    }

    private AccountResponse toResponse(Account account) {
        return new AccountResponse(account.getId(), account.getUser().getId(),
                account.getCurrency(), account.getBalance());
    }
}
