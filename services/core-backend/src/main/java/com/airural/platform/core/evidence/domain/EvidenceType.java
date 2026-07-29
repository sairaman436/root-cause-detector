/*
 * Purpose: Defines supported evidence asset categories.
 * Why it exists: Evidence validation, search, and future AI processing need a stable high-level type separate from MIME type.
 * Architecture fit: Domain vocabulary for the Evidence and Asset Management module.
 */
package com.airural.platform.core.evidence.domain;

/** Supported evidence asset categories. */
public enum EvidenceType {
    IMAGE,
    VIDEO,
    AUDIO,
    PDF,
    OFFICE_DOCUMENT,
    GPS_FILE,
    GENERIC_ATTACHMENT
}
