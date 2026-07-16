package com.examenc5.cti.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;

import org.junit.jupiter.api.Test;

import com.examenc5.cti.domain.AgentStatus;
import com.examenc5.cti.domain.CallStatus;
import com.examenc5.cti.domain.CtiEvent;
import com.examenc5.cti.domain.EventType;
import com.examenc5.cti.domain.ExtensionStatus;

class CtiStateServiceTests {

	private final CtiStateService stateService = new CtiStateService();

	@Test
	void updatesCallAgentAndExtensionState() {
		Instant now = Instant.parse("2026-07-16T10:00:00Z");

		stateService.apply(event(EventType.CALL_RECEIVED, now));
		assertThat(stateService.activeCalls()).singleElement()
				.extracting("status")
				.isEqualTo(CallStatus.RINGING);
		assertThat(stateService.agents()).singleElement()
				.extracting("status")
				.isEqualTo(AgentStatus.RINGING);
		assertThat(stateService.extensions()).singleElement()
				.extracting("status")
				.isEqualTo(ExtensionStatus.RINGING);

		stateService.apply(event(EventType.CALL_ANSWERED, now.plusSeconds(1)));
		assertThat(stateService.activeCalls()).singleElement()
				.extracting("status")
				.isEqualTo(CallStatus.IN_CALL);

		stateService.apply(event(EventType.CALL_HOLD, now.plusSeconds(2)));
		assertThat(stateService.agents()).singleElement()
				.extracting("status")
				.isEqualTo(AgentStatus.ON_HOLD);

		stateService.apply(event(EventType.CALL_ENDED, now.plusSeconds(3)));
		assertThat(stateService.activeCalls()).isEmpty();
		assertThat(stateService.agents()).singleElement()
				.extracting("status")
				.isEqualTo(AgentStatus.AVAILABLE);
		assertThat(stateService.extensions()).singleElement()
				.extracting("status")
				.isEqualTo(ExtensionStatus.IDLE);
	}

	private CtiEvent event(EventType eventType, Instant timestamp) {
		return new CtiEvent(
				eventType,
				"CALL-1001",
				"101",
				"A-100",
				"+1-555-123-4567",
				timestamp,
				null,
				null,
				null,
				null);
	}
}
