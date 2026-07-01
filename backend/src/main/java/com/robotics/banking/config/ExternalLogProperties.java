package com.robotics.banking.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "external.log")
public record ExternalLogProperties(boolean enabled, String url, Duration connectTimeout, Duration readTimeout) {
}
