package com.examenc5.cti.domain;

import java.time.Instant;

public record ConnectionSnapshot(
		boolean connected,
		String message,
		Instant updatedAt) {
}
