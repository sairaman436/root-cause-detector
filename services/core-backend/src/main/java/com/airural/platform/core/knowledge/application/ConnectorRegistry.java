/*
 * Purpose: Resolves source connectors by connector type.
 * Why it exists: Acquisition services need a single extension point for future crawlers and upload adapters.
 * Architecture fit: Application component implementing the AI-2 connector framework.
 */
package com.airural.platform.core.knowledge.application;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/** Registry for knowledge source connectors. */
@Component
public class ConnectorRegistry {
    private final List<SourceConnector> connectors;

    public ConnectorRegistry(List<SourceConnector> connectors) {
        this.connectors = connectors;
    }

    /** Resolves a connector by type or raises a governed configuration error. */
    public SourceConnector resolve(String connectorType) {
        return connectors.stream()
                .filter(connector -> connector.supports(connectorType))
                .findFirst()
                .orElseThrow(() -> new KnowledgeException(HttpStatus.BAD_REQUEST, "KNOWLEDGE_CONNECTOR_NOT_FOUND", "No connector is registered for type " + connectorType));
    }
}
