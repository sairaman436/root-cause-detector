/*
 * Purpose: Defines storage operations for evidence binaries.
 * Why it exists: Evidence must support local filesystem now and cloud object stores later without changing domain logic.
 * Architecture fit: Application port implemented by infrastructure storage adapters.
 */
package com.airural.platform.core.evidence.application;

import java.time.Duration;
import java.util.Optional;

/** Storage abstraction for evidence binaries. */
public interface EvidenceStorageService {
    StoredEvidenceObject store(byte[] content, String sanitizedFileName);

    EvidenceBinary load(String storageKey, String mimeType, String fileName);

    Optional<String> createSignedUrl(String storageKey, Duration ttl);
}
