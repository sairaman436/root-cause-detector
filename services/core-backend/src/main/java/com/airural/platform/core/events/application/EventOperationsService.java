/*
 * Purpose: Implements event operations APIs for logs, outbox, dead-letter, replay, audit, and subscriptions.
 * Why it exists: Platform operators need secure control-plane workflows for event streaming infrastructure.
 * Architecture fit: Application service for Milestone 7 Event Streaming and Data Integration.
 */
package com.airural.platform.core.events.application;

import com.airural.platform.core.events.domain.*;
import com.airural.platform.core.events.infrastructure.*;
import com.airural.platform.core.events.web.dto.EventDtos.*;
import java.time.Instant;
import java.util.*;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/** Transactional event operations service. */
@Service
public class EventOperationsService {
    private final EventLogRepository eventLogRepository;
    private final OutboxEventRepository outboxRepository;
    private final DeadLetterEventRepository deadLetterRepository;
    private final EventSubscriptionRepository subscriptionRepository;
    private final EventMapper mapper;

    public EventOperationsService(
            EventLogRepository eventLogRepository,
            OutboxEventRepository outboxRepository,
            DeadLetterEventRepository deadLetterRepository,
            EventSubscriptionRepository subscriptionRepository,
            EventMapper mapper) {
        this.eventLogRepository = eventLogRepository;
        this.outboxRepository = outboxRepository;
        this.deadLetterRepository = deadLetterRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public Page<EventResponse> events(Pageable pageable) {
        return eventLogRepository.findAll(pageable).map(mapper::event);
    }

    @Transactional(readOnly = true)
    public EventResponse event(UUID id) {
        EventLogEntity entity = eventLogRepository.findById(id)
                .or(() -> eventLogRepository.findByEventId(id))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event was not found"));
        return mapper.event(entity);
    }

    @Transactional(readOnly = true)
    public Page<OutboxResponse> outbox(Pageable pageable) {
        return outboxRepository.findAll(pageable).map(mapper::outbox);
    }

    @Transactional(readOnly = true)
    public Page<DeadLetterResponse> deadLetters(Pageable pageable) {
        return deadLetterRepository.findAll(pageable).map(mapper::deadLetter);
    }

    @Transactional(readOnly = true)
    public Page<EventResponse> audit(Pageable pageable) {
        return eventLogRepository.findAll((root, query, cb) -> cb.equal(root.get("topic"), "audit.created"), pageable).map(mapper::event);
    }

    @Transactional(readOnly = true)
    public Page<EventSubscriptionResponse> subscriptions(Pageable pageable) {
        return subscriptionRepository.findAll(pageable).map(mapper::subscription);
    }

    @Transactional
    public OutboxResponse replay(UUID eventId) {
        OutboxEventEntity outbox = outboxRepository.findById(eventId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Outbox event was not found"));
        outbox.markRetryPending();
        deadLetterRepository.findAll().stream()
                .filter(deadLetter -> deadLetter.eventId().equals(eventId))
                .forEach(DeadLetterEventEntity::markReplayed);
        return mapper.outbox(outbox);
    }
}
