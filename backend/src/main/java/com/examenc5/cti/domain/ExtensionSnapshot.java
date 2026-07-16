package com.examenc5.cti.domain;

import java.time.Instant;

public record ExtensionSnapshot(
		String extension,
		ExtensionStatus status,
		String currentCallId,
		Instant updatedAt) {
}
