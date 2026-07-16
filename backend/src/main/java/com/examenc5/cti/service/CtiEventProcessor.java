package com.examenc5.cti.service;

import java.time.Instant;
import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.examenc5.cti.domain.CtiEvent;
import com.examenc5.cti.domain.EventType;

@Service
public class CtiEventProcessor {

	private static final Logger log = LoggerFactory.getLogger(CtiEventProcessor.class);
	private static final Set<EventType> CALL_EVENTS = EnumSet.of(
			EventType.CALL_RECEIVED,
			EventType.CALL_ANSWERED,
			EventType.CALL_HOLD,
			EventType.CALL_RESUME,
			EventType.CALL_TRANSFER,
			EventType.CALL_ENDED);

	private final CtiStateService stateService;
	private final RealtimeEventService realtimeEventService;
	private final Set<String> processedEvents = ConcurrentHashMap.newKeySet();

	public CtiEventProcessor(CtiStateService stateService, RealtimeEventService realtimeEventService) {
		this.stateService = stateService;
		this.realtimeEventService = realtimeEventService;
	}

	public void process(CtiEvent event) {
		if (event == null || event.eventType() == null) {
			log.warn("Ignoring invalid CTI event: {}", event);
			return;
		}

		if (isCallEvent(event.eventType()) && isBlank(event.callId())) {
			log.warn("Ignoring call event without callId: {}", event);
			return;
		}

		String eventKey = eventKey(event);
		if (!processedEvents.add(eventKey)) {
			log.info("Duplicated CTI event ignored: {}", eventKey);
			return;
		}
		if (processedEvents.size() > 2_000) {
			processedEvents.clear();
		}

		log.info("CTI event received: type={}, callId={}, extension={}, agent={}",
				event.eventType(), event.callId(), event.extension(), event.agentId());

		stateService.apply(event);
		realtimeEventService.broadcast(stateService.snapshot());
	}

	public void connected(String message) {
		stateService.connectionChanged(true, message);
		realtimeEventService.broadcast(stateService.snapshot());
	}

	public void disconnected(String message) {
		stateService.connectionChanged(false, message);
		realtimeEventService.broadcast(stateService.snapshot());
	}

	private boolean isCallEvent(EventType eventType) {
		return CALL_EVENTS.contains(eventType);
	}

	private String eventKey(CtiEvent event) {
		Instant timestamp = event.timestamp() != null ? event.timestamp() : event.serverTime();
		return event.eventType() + "|" + event.callId() + "|" + timestamp;
	}

	private boolean isBlank(String value) {
		return value == null || value.isBlank();
	}
}
