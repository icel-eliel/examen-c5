package com.examenc5.cti.domain;

import java.time.Instant;

public record CallSnapshot(
		String callId,
		String extension,
		String agentId,
		String phoneNumber,
		CallStatus status,
		EventType lastEventType,
		Instant updatedAt) {
}
