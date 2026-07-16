package com.examenc5.cti.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "cti")
public record CtiProperties(
		String url,
		boolean enabled,
		Duration reconnectInitialDelay,
		Duration reconnectMaxDelay) {
}
