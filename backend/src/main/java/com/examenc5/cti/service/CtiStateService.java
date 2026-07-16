package com.examenc5.cti.service;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.examenc5.cti.domain.AgentSnapshot;
import com.examenc5.cti.domain.AgentStatus;
import com.examenc5.cti.domain.CallSnapshot;
import com.examenc5.cti.domain.CallStatus;
import com.examenc5.cti.domain.ConnectionSnapshot;
import com.examenc5.cti.domain.CtiEvent;
import com.examenc5.cti.domain.DashboardSnapshot;
import com.examenc5.cti.domain.EventType;
import com.examenc5.cti.domain.ExtensionSnapshot;
import com.examenc5.cti.domain.ExtensionStatus;

@Service
public class CtiStateService {

	private final Map<String, CallSnapshot> activeCalls = new ConcurrentHashMap<>();
	private final Map<String, AgentSnapshot> agents = new ConcurrentHashMap<>();
	private final Map<String, ExtensionSnapshot> extensions = new ConcurrentHashMap<>();
	private volatile ConnectionSnapshot connection = new ConnectionSnapshot(false, "Not connected", Instant.now());

	public synchronized void apply(CtiEvent event) {
		if (event.eventType() == EventType.CTI_CONNECTED) {
			connectionChanged(true, "Connected to " + valueOrDefault(event.server(), "CTI server"));
			return;
		}
		if (event.eventType() == EventType.HEARTBEAT) {
			connectionChanged(true, "Heartbeat received");
			return;
		}

		rememberResources(event);

		if (event.eventType() == EventType.CALL_ENDED) {
			activeCalls.remove(event.callId());
		} else {
			activeCalls.put(event.callId(), toCallSnapshot(event));
		}

		rebuildResourceStates();
	}

	public void connectionChanged(boolean connected, String message) {
		this.connection = new ConnectionSnapshot(connected, message, Instant.now());
	}

	public List<CallSnapshot> activeCalls() {
		return activeCalls.values().stream()
				.sorted(Comparator.comparing(CallSnapshot::updatedAt).reversed())
				.toList();
	}

	public List<AgentSnapshot> agents() {
		return agents.values().stream()
				.sorted(Comparator.comparing(AgentSnapshot::agentId))
				.toList();
	}

	public List<ExtensionSnapshot> extensions() {
		return extensions.values().stream()
				.sorted(Comparator.comparing(ExtensionSnapshot::extension))
				.toList();
	}

	public ConnectionSnapshot connection() {
		return connection;
	}

	public DashboardSnapshot snapshot() {
		return new DashboardSnapshot(connection(), activeCalls(), agents(), extensions(), Instant.now());
	}

	private void rememberResources(CtiEvent event) {
		Instant now = timestampOf(event);
		if (hasText(event.agentId())) {
			agents.putIfAbsent(event.agentId(), new AgentSnapshot(event.agentId(), AgentStatus.UNKNOWN, null, now));
		}
		if (hasText(event.extension())) {
			extensions.putIfAbsent(event.extension(), new ExtensionSnapshot(event.extension(), ExtensionStatus.UNKNOWN, null, now));
		}
	}

	private CallSnapshot toCallSnapshot(CtiEvent event) {
		return new CallSnapshot(
				event.callId(),
				event.extension(),
				event.agentId(),
				event.phoneNumber(),
				callStatusFor(event.eventType()),
				event.eventType(),
				timestampOf(event));
	}

	private void rebuildResourceStates() {
		agents.replaceAll((agentId, current) -> statusForAgent(agentId, current));
		extensions.replaceAll((extension, current) -> statusForExtension(extension, current));
	}

	private AgentSnapshot statusForAgent(String agentId, AgentSnapshot current) {
		return activeCalls.values().stream()
				.filter(call -> agentId.equals(call.agentId()))
				.max(Comparator.comparingInt(call -> agentPriority(call.status())))
				.map(call -> new AgentSnapshot(agentId, agentStatusFor(call.status()), call.callId(), call.updatedAt()))
				.orElse(new AgentSnapshot(agentId, AgentStatus.AVAILABLE, null, Instant.now()));
	}

	private ExtensionSnapshot statusForExtension(String extension, ExtensionSnapshot current) {
		return activeCalls.values().stream()
				.filter(call -> extension.equals(call.extension()))
				.max(Comparator.comparingInt(call -> extensionPriority(call.status())))
				.map(call -> new ExtensionSnapshot(extension, extensionStatusFor(call.status()), call.callId(), call.updatedAt()))
				.orElse(new ExtensionSnapshot(extension, ExtensionStatus.IDLE, null, Instant.now()));
	}

	private CallStatus callStatusFor(EventType eventType) {
		return switch (eventType) {
			case CALL_RECEIVED -> CallStatus.RINGING;
			case CALL_ANSWERED, CALL_RESUME -> CallStatus.IN_CALL;
			case CALL_HOLD -> CallStatus.ON_HOLD;
			case CALL_TRANSFER -> CallStatus.TRANSFERRED;
			default -> CallStatus.IN_CALL;
		};
	}

	private AgentStatus agentStatusFor(CallStatus status) {
		return switch (status) {
			case RINGING -> AgentStatus.RINGING;
			case ON_HOLD -> AgentStatus.ON_HOLD;
			case IN_CALL, TRANSFERRED -> AgentStatus.BUSY;
		};
	}

	private ExtensionStatus extensionStatusFor(CallStatus status) {
		return switch (status) {
			case RINGING -> ExtensionStatus.RINGING;
			case ON_HOLD -> ExtensionStatus.ON_HOLD;
			case IN_CALL, TRANSFERRED -> ExtensionStatus.IN_USE;
		};
	}

	private int agentPriority(CallStatus status) {
		return switch (status) {
			case ON_HOLD -> 4;
			case IN_CALL, TRANSFERRED -> 3;
			case RINGING -> 2;
		};
	}

	private int extensionPriority(CallStatus status) {
		return agentPriority(status);
	}

	private Instant timestampOf(CtiEvent event) {
		if (event.timestamp() != null) {
			return event.timestamp();
		}
		if (event.serverTime() != null) {
			return event.serverTime();
		}
		return Instant.now();
	}

	private String valueOrDefault(String value, String defaultValue) {
		return hasText(value) ? value : defaultValue;
	}

	private boolean hasText(String value) {
		return value != null && !value.isBlank();
	}
}
