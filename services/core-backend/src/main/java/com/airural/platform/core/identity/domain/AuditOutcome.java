/*
 * Purpose: Defines audit event outcomes.
 * Why it exists: Gives audit records a normalized success/failure field.
 * Architecture fit: Supports security monitoring and compliance reporting.
 */
package com.airural.platform.core.identity.domain;

/** Outcome for auditable security and identity events. */
public enum AuditOutcome {
    /** Event completed successfully. */
    SUCCESS,
    /** Event failed or was denied. */
    FAILURE
}
