/*
 * Purpose: Defines transactional outbox publication states.
 * Why it exists: Reliable event publishing requires explicit state transitions and retry handling.
 * Architecture fit: Domain vocabulary for the eventing module.
 */
package com.airural.platform.core.events.domain;

/** Outbox event publication status. */
public enum OutboxStatus {
    PENDING,
    PUBLISHED,
    FAILED,
    DEAD_LETTERED
}
