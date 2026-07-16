package com.examenc5.cti.web;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.examenc5.cti.domain.AgentSnapshot;
import com.examenc5.cti.domain.CallSnapshot;
import com.examenc5.cti.domain.ConnectionSnapshot;
import com.examenc5.cti.domain.ExtensionSnapshot;
import com.examenc5.cti.service.CtiStateService;
import com.examenc5.cti.service.RealtimeEventService;

@RestController
public class CtiController {

	private final CtiStateService stateService;
	private final RealtimeEventService realtimeEventService;

	public CtiController(CtiStateService stateService, RealtimeEventService realtimeEventService) {
		this.stateService = stateService;
		this.realtimeEventService = realtimeEventService;
	}

	@GetMapping("/calls/active")
	public List<CallSnapshot> activeCalls() {
		return stateService.activeCalls();
	}

	@GetMapping("/agents")
	public List<AgentSnapshot> agents() {
		return stateService.agents();
	}

	@GetMapping("/extensions")
	public List<ExtensionSnapshot> extensions() {
		return stateService.extensions();
	}

	@GetMapping("/cti/connection")
	public ConnectionSnapshot connection() {
		return stateService.connection();
	}

	@GetMapping("/stream/cti")
	public SseEmitter stream() {
		return realtimeEventService.subscribe(stateService.snapshot());
	}
}
