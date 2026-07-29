/*
 * Purpose: Describes a stored evidence binary.
 * Why it exists: Application services need storage metadata without depending on a concrete storage provider.
 * Architecture fit: Boundary contract between evidence use cases and storage adapters.
 */
package com.airural.platform.core.evidence.application;

import com.airural.platform.core.evidence.domain.StorageProvider;

/** Result returned after storing an evidence binary. */
public record StoredEvidenceObject(StorageProvider provider, String storageKey, String storedFileName) {
}
