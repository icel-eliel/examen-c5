package com.examenc5.cti.domain;

import java.time.Instant;

public record CtiEvent(
		EventType eventType,
		String callId,
		String extension,
		String agentId,
		String phoneNumber,
		Instant timestamp,
		String server,
		String version,
		String description,
		Instant serverTime) {
}
