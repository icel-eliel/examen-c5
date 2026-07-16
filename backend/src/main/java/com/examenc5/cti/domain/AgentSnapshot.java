package com.examenc5.cti.domain;

import java.time.Instant;

public record AgentSnapshot(
		String agentId,
		AgentStatus status,
		String currentCallId,
		Instant updatedAt) {
}
