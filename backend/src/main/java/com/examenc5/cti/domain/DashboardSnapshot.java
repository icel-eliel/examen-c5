package com.examenc5.cti.domain;

import java.time.Instant;
import java.util.List;

public record DashboardSnapshot(
		ConnectionSnapshot connection,
		List<CallSnapshot> activeCalls,
		List<AgentSnapshot> agents,
		List<ExtensionSnapshot> extensions,
		Instant generatedAt) {
}
