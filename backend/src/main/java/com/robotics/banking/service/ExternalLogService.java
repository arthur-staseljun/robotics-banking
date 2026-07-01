package com.robotics.banking.service;

import com.robotics.banking.config.ExternalLogProperties;
import com.robotics.banking.exception.ExternalLogException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
@RequiredArgsConstructor
public class ExternalLogService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExternalLogService.class);

    private final RestClient externalLogRestClient;
    private final ExternalLogProperties properties;

    public void logBeforeDebit(Long accountId) {
        if (!properties.enabled()) {
            LOGGER.debug("External logging disabled; skipping call for account {}", accountId);
            return;
        }
        try {
            externalLogRestClient.get()
                    .uri(properties.url())
                    .retrieve()
                    .toBodilessEntity();
            LOGGER.debug("External log call succeeded for account {}", accountId);
        } catch (Exception e) {
            throw new ExternalLogException(
                    "External logging call failed for account " + accountId + "; debit rejected", e);
        }
    }
}
