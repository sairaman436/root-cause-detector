/*
 * Purpose: Converts typed shared event payload records into JSON payload maps.
 * Why it exists: Outbox records store immutable JSON payloads while producers use versioned shared contracts.
 * Architecture fit: Application helper for the event contract boundary.
 */
package com.airural.platform.core.events.application;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.springframework.stereotype.Component;

/** Converts event payload contracts to map payloads. */
@Component
public class EventPayloadMapper {
    private final ObjectMapper objectMapper;

    public EventPayloadMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /** Converts a typed payload to a JSON-compatible map. */
    public Map<String, Object> toMap(Object payload) {
        return objectMapper.convertValue(payload, new TypeReference<>() {
        });
    }
}
