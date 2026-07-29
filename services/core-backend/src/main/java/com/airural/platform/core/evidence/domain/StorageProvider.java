/*
 * Purpose: Identifies the storage backend that owns an evidence binary.
 * Why it exists: The platform must support local filesystem, S3-compatible, Azure Blob, and Google Cloud Storage adapters.
 * Architecture fit: Domain-level storage provider metadata used by storage abstraction implementations.
 */
package com.airural.platform.core.evidence.domain;

/** Storage backends supported by the evidence storage abstraction. */
public enum StorageProvider {
    LOCAL,
    S3_COMPATIBLE,
    AZURE_BLOB,
    GOOGLE_CLOUD_STORAGE
}
