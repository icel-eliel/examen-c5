package com.examenc5.cti.integration;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.time.Duration;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

import com.examenc5.cti.config.CtiProperties;
import com.examenc5.cti.domain.CtiEvent;
import com.examenc5.cti.service.CtiEventProcessor;
import tools.jackson.databind.ObjectMapper;

@Component
public class CtiWebSocketClient implements SmartLifecycle {

	private static final Logger log = LoggerFactory.getLogger(CtiWebSocketClient.class);

	private final CtiProperties properties;
	private final ObjectMapper objectMapper;
	private final CtiEventProcessor eventProcessor;
	private final HttpClient httpClient;
	private final ScheduledExecutorService reconnectExecutor;
	private final AtomicBoolean running = new AtomicBoolean(false);
	private final AtomicReference<WebSocket> webSocket = new AtomicReference<>();
	private final AtomicInteger reconnectAttempts = new AtomicInteger(0);

	public CtiWebSocketClient(CtiProperties properties, ObjectMapper objectMapper, CtiEventProcessor eventProcessor) {
		this.properties = properties;
		this.objectMapper = objectMapper;
		this.eventProcessor = eventProcessor;
		this.httpClient = HttpClient.newBuilder()
				.connectTimeout(Duration.ofSeconds(10))
				.build();
		this.reconnectExecutor = Executors.newSingleThreadScheduledExecutor(task -> {
			Thread thread = new Thread(task, "cti-reconnect");
			thread.setDaemon(true);
			return thread;
		});
	}

	@Override
	public void start() {
		if (!properties.enabled()) {
			log.warn("CTI WebSocket client is disabled by configuration");
			return;
		}
		if (running.compareAndSet(false, true)) {
			connect();
		}
	}

	@Override
	public void stop() {
		running.set(false);
		WebSocket current = webSocket.getAndSet(null);
		if (current != null) {
			current.sendClose(WebSocket.NORMAL_CLOSURE, "Application stopping");
		}
		reconnectExecutor.shutdownNow();
	}

	@Override
	public boolean isRunning() {
		return running.get();
	}

	private void connect() {
		if (!running.get()) {
			return;
		}

		log.info("Connecting to CTI WebSocket: {}", properties.url());
		httpClient.newWebSocketBuilder()
				.header("ngrok-skip-browser-warning", "true")
				.buildAsync(URI.create(properties.url()), new Listener())
				.whenComplete((socket, error) -> {
					if (error != null) {
						log.error("Could not connect to CTI WebSocket: {}", error.getMessage());
						eventProcessor.disconnected("Connection failed: " + error.getMessage());
						scheduleReconnect();
						return;
					}
					webSocket.set(socket);
				});
	}

	private void scheduleReconnect() {
		if (!running.get()) {
			return;
		}
		int attempt = reconnectAttempts.incrementAndGet();
		long delaySeconds = Math.min(
				properties.reconnectInitialDelay().toSeconds() * attempt,
				properties.reconnectMaxDelay().toSeconds());
		log.info("Scheduling CTI reconnect attempt {} in {} seconds", attempt, delaySeconds);
		reconnectExecutor.schedule(this::connect, delaySeconds, TimeUnit.SECONDS);
	}

	private class Listener implements WebSocket.Listener {

		private final StringBuilder partialMessage = new StringBuilder();

		@Override
		public void onOpen(WebSocket webSocket) {
			reconnectAttempts.set(0);
			eventProcessor.connected("Connected to CTI WebSocket");
			log.info("Connected to CTI WebSocket");
			WebSocket.Listener.super.onOpen(webSocket);
		}

		@Override
		public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
			partialMessage.append(data);
			if (last) {
				String payload = partialMessage.toString();
				partialMessage.setLength(0);
				handleMessage(payload);
			}
			webSocket.request(1);
			return null;
		}

		@Override
		public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
			eventProcessor.disconnected("Connection closed: " + statusCode + " " + reason);
			log.warn("CTI WebSocket closed. status={}, reason={}", statusCode, reason);
			scheduleReconnect();
			return WebSocket.Listener.super.onClose(webSocket, statusCode, reason);
		}

		@Override
		public void onError(WebSocket webSocket, Throwable error) {
			eventProcessor.disconnected("Connection error: " + error.getMessage());
			log.error("CTI WebSocket error: {}", error.getMessage());
			scheduleReconnect();
		}

		private void handleMessage(String payload) {
			try {
				CtiEvent event = objectMapper.readValue(payload, CtiEvent.class);
				eventProcessor.process(event);
			} catch (Exception ex) {
				log.warn("Invalid CTI payload received: {}", payload, ex);
			}
		}
	}
}
