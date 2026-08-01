/*
 * Purpose: Provides a deterministic connector for trusted web and government sources.
 * Why it exists: The platform needs a production extension point while keeping this milestone free of live crawling side effects.
 * Architecture fit: Connector implementation for AI-2 source discovery.
 */
package com.airural.platform.core.knowledge.application;

import java.util.List;
import org.springframework.stereotype.Component;

/** Trusted HTTP-style source connector. */
@Component
public class TrustedHttpConnector implements SourceConnector {
    @Override
    public boolean supports(String connectorType) {
        return "TRUSTED_HTTP".equalsIgnoreCase(connectorType) || "GOVERNMENT_PORTAL".equalsIgnoreCase(connectorType);
    }

    @Override
    public List<DiscoveredDocument> discover(String sourceKey, String incrementalCursor) {
        return List.of(new DiscoveredDocument(sourceKey + " discovery manifest", "HTML", sourceKey + ":" + (incrementalCursor == null ? "initial" : incrementalCursor)));
    }
}
