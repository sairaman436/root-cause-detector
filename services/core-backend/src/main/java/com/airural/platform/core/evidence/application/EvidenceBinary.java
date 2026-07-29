/*
 * Purpose: Carries downloaded evidence binary data.
 * Why it exists: Download controllers need bytes and content metadata independent of storage implementation.
 * Architecture fit: Storage abstraction response contract.
 */
package com.airural.platform.core.evidence.application;

/** Evidence binary payload loaded from storage. */
public record EvidenceBinary(byte[] content, String mimeType, String fileName) {
}
