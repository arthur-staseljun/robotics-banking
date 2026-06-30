package com.robotics.banking.service;

import com.robotics.banking.dto.request.DebitRequest;
import com.robotics.banking.dto.response.TransactionResponse;
import com.robotics.banking.enums.Currency;
import com.robotics.banking.enums.TransactionType;
import com.robotics.banking.exception.ExternalLogException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DebitServiceTest {

    @Mock
    private ExternalLogService externalLogService;

    @Mock
    private AccountService accountService;

    @InjectMocks
    private DebitService debitService;

    @Test
    void callsExternalLogBeforeDebitingAndReturnsResult() {
        DebitRequest request = new DebitRequest(new BigDecimal("10.00"), "rent");
        TransactionResponse expected = new TransactionResponse(1L, 1L, TransactionType.DEBIT,
                new BigDecimal("10.00"), Currency.EUR, new BigDecimal("90.00"), "rent", Instant.now());
        when(accountService.debit(eq(1L), any())).thenReturn(expected);

        TransactionResponse result = debitService.debit(1L, request);

        assertThat(result).isSameAs(expected);
        InOrder order = inOrder(externalLogService, accountService);
        order.verify(externalLogService).logBeforeDebit(1L);
        order.verify(accountService).debit(1L, request);
    }

    @Test
    void doesNotDebitWhenExternalLogFails() {
        DebitRequest request = new DebitRequest(new BigDecimal("10.00"), null);
        doThrow(new ExternalLogException("boom", new RuntimeException()))
                .when(externalLogService).logBeforeDebit(1L);

        assertThatThrownBy(() -> debitService.debit(1L, request))
                .isInstanceOf(ExternalLogException.class);

        verifyNoInteractions(accountService);
    }
}
