package com.examenc5.cti.service;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.examenc5.cti.domain.DashboardSnapshot;

@Service
public class RealtimeEventService {

	private static final Logger log = LoggerFactory.getLogger(RealtimeEventService.class);
	private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

	public SseEmitter subscribe(DashboardSnapshot initialSnapshot) {
		SseEmitter emitter = new SseEmitter(0L);
		emitters.add(emitter);
		emitter.onCompletion(() -> emitters.remove(emitter));
		emitter.onTimeout(() -> emitters.remove(emitter));
		emitter.onError(error -> emitters.remove(emitter));
		send(emitter, initialSnapshot);
		return emitter;
	}

	public void broadcast(DashboardSnapshot snapshot) {
		for (SseEmitter emitter : emitters) {
			send(emitter, snapshot);
		}
	}

	private void send(SseEmitter emitter, DashboardSnapshot snapshot) {
		try {
			emitter.send(SseEmitter.event()
					.name("cti-snapshot")
					.data(snapshot));
		} catch (IOException | IllegalStateException ex) {
			log.debug("Removing disconnected SSE client");
			emitters.remove(emitter);
		}
	}
}
