package com.robotics.banking.config;

import com.robotics.banking.enums.Currency;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;
import java.util.Map;

@ConfigurationProperties(prefix = "exchange")
public record ExchangeRateProperties(Currency base, Map<Currency, BigDecimal> rates) {
}
