/*
 * Purpose: Provides the connector path for uploaded documents and internal corpora.
 * Why it exists: Village documents, historical reports, and manually uploaded policies must enter the same governed acquisition pipeline.
 * Architecture fit: Connector implementation for AI-2 uploaded document acquisition.
 */
package com.airural.platform.core.knowledge.application;

import java.util.List;
import org.springframework.stereotype.Component;

/** Uploaded document connector. */
@Component
public class UploadedDocumentConnector implements SourceConnector {
    @Override
    public boolean supports(String connectorType) {
        return "UPLOAD".equalsIgnoreCase(connectorType) || "UPLOADED_DOCUMENT".equalsIgnoreCase(connectorType);
    }

    @Override
    public List<DiscoveredDocument> discover(String sourceKey, String incrementalCursor) {
        return List.of(new DiscoveredDocument(sourceKey + " uploaded document batch", "UPLOAD_MANIFEST", sourceKey + ":upload:" + (incrementalCursor == null ? "initial" : incrementalCursor)));
    }
}
