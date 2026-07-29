/*
 * Purpose: Exposes event streaming control-plane APIs.
 * Why it exists: Event administrators and viewers need secure access to event logs, outbox state, dead-letter queues, replay, and audit event history.
 * Architecture fit: REST adapter for Milestone 7 Event Streaming and Data Integration.
 */
package com.airural.platform.core.events.web;

import com.airural.platform.core.common.*;
import com.airural.platform.core.events.application.EventOperationsService;
import com.airural.platform.core.events.web.dto.EventDtos.*;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** REST controller for event operations. */
@RestController
@RequestMapping("/api/v1/events")
public class EventController {
    private final EventOperationsService service;

    public EventController(EventOperationsService service) {
        this.service = service;
    }

    @Operation(summary = "List events", description = "Lists immutable event log records.")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<EventResponse>>> events(Pageable pageable, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.events(pageable), RequestIds.from(request)));
    }

    @Operation(summary = "Get event", description = "Gets an event by event-log ID or event ID.")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EventResponse>> event(@PathVariable java.util.UUID id, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.event(id), RequestIds.from(request)));
    }

    @Operation(summary = "List outbox", description = "Lists transactional outbox records and publication state.")
    @GetMapping("/outbox")
    public ResponseEntity<ApiResponse<Page<OutboxResponse>>> outbox(Pageable pageable, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.outbox(pageable), RequestIds.from(request)));
    }

    @Operation(summary = "List dead-letter events", description = "Lists events that exceeded retry limits or failed processing.")
    @GetMapping("/dead-letter")
    public ResponseEntity<ApiResponse<Page<DeadLetterResponse>>> deadLetters(Pageable pageable, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.deadLetters(pageable), RequestIds.from(request)));
    }

    @Operation(summary = "Replay event", description = "Returns a dead-lettered or failed outbox event to pending state for replay.")
    @PostMapping("/replay")
    public ResponseEntity<ApiResponse<OutboxResponse>> replay(@Valid @RequestBody ReplayEventRequest body, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.replay(body.eventId()), RequestIds.from(request)));
    }

    @Operation(summary = "List audit events", description = "Lists event-backed audit records.")
    @GetMapping("/audit")
    public ResponseEntity<ApiResponse<Page<EventResponse>>> audit(Pageable pageable, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.audit(pageable), RequestIds.from(request)));
    }

    @Operation(summary = "List subscriptions", description = "Lists active and placeholder event consumer subscriptions.")
    @GetMapping("/subscriptions")
    public ResponseEntity<ApiResponse<Page<EventSubscriptionResponse>>> subscriptions(Pageable pageable, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.subscriptions(pageable), RequestIds.from(request)));
    }
}
