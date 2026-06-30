package com.robotics.banking.dto.request;

import com.robotics.banking.enums.Currency;
import jakarta.validation.constraints.NotNull;

public record CreateAccountRequest(
        @NotNull Currency currency) {
}
