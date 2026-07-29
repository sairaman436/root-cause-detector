/*
 * Purpose: Defines evidence audit actions.
 * Why it exists: Evidence operations require module-level auditability in addition to platform-wide audit logs.
 * Architecture fit: Domain vocabulary for immutable evidence audit events.
 */
package com.airural.platform.core.evidence.domain;

/** Audited evidence lifecycle actions. */
public enum EvidenceAuditAction {
    UPLOADED,
    DOWNLOADED,
    METADATA_UPDATED,
    SOFT_DELETED,
    RESTORED,
    SIGNED_URL_REQUESTED
}
