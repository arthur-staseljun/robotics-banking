package com.robotics.banking.controller;

import com.robotics.banking.dto.response.AccountResponse;
import com.robotics.banking.model.Currency;
import com.robotics.banking.service.AccountService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AccountController.class)
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AccountService accountService;

    @Test
    void createAccountReturns201WithBody() throws Exception {
        when(accountService.create(eq(1L), any()))
                .thenReturn(new AccountResponse(10L, 1L, Currency.EUR, new BigDecimal("0.0000")));

        mockMvc.perform(post("/api/users/1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"currency\":\"EUR\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.currency").value("EUR"));
    }

    @Test
    void createAccountWithMissingCurrencyReturns400() throws Exception {
        mockMvc.perform(post("/api/users/1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAccountReturns200() throws Exception {
        when(accountService.get(10L))
                .thenReturn(new AccountResponse(10L, 1L, Currency.USD, new BigDecimal("42.5000")));

        mockMvc.perform(get("/api/accounts/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currency").value("USD"))
                .andExpect(jsonPath("$.balance").value(42.5));
    }

    @Test
    void getMissingAccountReturns404() throws Exception {
        when(accountService.get(99L))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Account 99 not found"));

        mockMvc.perform(get("/api/accounts/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void listByUserReturnsArray() throws Exception {
        when(accountService.listByUser(1L)).thenReturn(List.of(
                new AccountResponse(10L, 1L, Currency.EUR, new BigDecimal("0.0000")),
                new AccountResponse(11L, 1L, Currency.USD, new BigDecimal("0.0000"))
        ));

        mockMvc.perform(get("/api/users/1/accounts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[1].currency").value("USD"));
    }
}
