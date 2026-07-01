package com.robotics.banking.controller;

import com.robotics.banking.dto.response.TransactionResponse;
import com.robotics.banking.enums.Currency;
import com.robotics.banking.enums.TransactionType;
import com.robotics.banking.service.TransactionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransactionController.class)
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransactionService transactionService;

    private static final Instant CREATED_AT = Instant.parse("2026-01-01T00:00:00Z");

    @Test
    void historyReturnsPageJson() throws Exception {
        when(transactionService.getHistory(eq(1L), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(
                new TransactionResponse(5L, 1L, TransactionType.DEPOSIT, new BigDecimal("100.0000"),
                        Currency.EUR, new BigDecimal("100.0000"), "salary", CREATED_AT, null))));

        mockMvc.perform(get("/api/accounts/1/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(5))
                .andExpect(jsonPath("$.content[0].type").value("DEPOSIT"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void getTransactionReturns200() throws Exception {
        when(transactionService.get(5L)).thenReturn(new TransactionResponse(5L, 1L, TransactionType.DEBIT,
                new BigDecimal("30.0000"), Currency.EUR, new BigDecimal("70.0000"), "rent", CREATED_AT, null));

        mockMvc.perform(get("/api/transactions/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.type").value("DEBIT"));
    }

    @Test
    void getMissingTransactionReturns404() throws Exception {
        when(transactionService.get(99L))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Transaction 99 not found"));

        mockMvc.perform(get("/api/transactions/99"))
                .andExpect(status().isNotFound());
    }
}
