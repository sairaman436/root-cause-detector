/*
 * Purpose: Defines canonical Kafka topic names for platform domain events.
 * Why it exists: Producers and consumers need one governed source of truth for topic naming.
 * Architecture fit: Shared event contract vocabulary for the enterprise event backbone.
 */
package com.airural.platform.shared.events;

/** Canonical Kafka topics used by the platform event bus. */
public enum EventTopic {
    SURVEY_CREATED("survey.created"),
    SURVEY_UPDATED("survey.updated"),
    SURVEY_SUBMITTED("survey.submitted"),
    SURVEY_COMPLETED("survey.completed"),
    EVIDENCE_UPLOADED("evidence.uploaded"),
    EVIDENCE_VALIDATED("evidence.validated"),
    KNOWLEDGE_DOCUMENT_UPLOADED("knowledge.document.uploaded"),
    KNOWLEDGE_DOCUMENT_UPDATED("knowledge.document.updated"),
    GEO_HOUSEHOLD_CREATED("geo.household.created"),
    GEO_INFRASTRUCTURE_UPDATED("geo.infrastructure.updated"),
    NOTIFICATION_CREATED("notification.created"),
    AUDIT_CREATED("audit.created"),
    USER_CREATED("user.created"),
    USER_UPDATED("user.updated"),
    USER_LOGGED_IN("user.logged-in");

    private final String topicName;

    EventTopic(String topicName) {
        this.topicName = topicName;
    }

    public String topicName() {
        return topicName;
    }

    public static EventTopic fromTopicName(String topicName) {
        for (EventTopic topic : values()) {
            if (topic.topicName.equals(topicName)) {
                return topic;
            }
        }
        throw new IllegalArgumentException("Unknown event topic: " + topicName);
    }
}
