package com.robotics.banking.dto.request;

import com.robotics.banking.model.Currency;
import jakarta.validation.constraints.NotNull;

public record CreateAccountRequest(
        @NotNull Currency currency) {
}
