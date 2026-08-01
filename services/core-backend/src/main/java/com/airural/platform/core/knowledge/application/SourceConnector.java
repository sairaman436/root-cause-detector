/*
 * Purpose: Defines the pluggable source connector contract.
 * Why it exists: Knowledge acquisition must support government, NGO, research, upload, and future source connectors without changing core workflow code.
 * Architecture fit: Application port for the AI-2 connector framework.
 */
package com.airural.platform.core.knowledge.application;

import java.util.List;

/** Connector contract for source-specific discovery. */
public interface SourceConnector {
    /** Returns true when this connector can handle the requested connector type. */
    boolean supports(String connectorType);

    /** Discovers source documents without performing external model training or fine-tuning. */
    List<DiscoveredDocument> discover(String sourceKey, String incrementalCursor);

    /** Lightweight discovered document projection. */
    record DiscoveredDocument(String title, String documentType, String contentFingerprint) {
    }
}
