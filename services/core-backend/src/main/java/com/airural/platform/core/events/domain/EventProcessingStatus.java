/*
 * Purpose: Defines consumer processing outcomes.
 * Why it exists: Consumers need stable status values for retry, observability, and dead-letter diagnostics.
 * Architecture fit: Domain vocabulary for event processing logs.
 */
package com.airural.platform.core.events.domain;

/** Event consumer processing status. */
public enum EventProcessingStatus {
    PROCESSED,
    FAILED,
    DEAD_LETTERED
}
