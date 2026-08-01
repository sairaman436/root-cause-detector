/*
 * Purpose: Manages conversation, session, task, village, survey, shared, and knowledge reference memory.
 * Why it exists: Agents need shared context without leaking internal service calls into agent logic.
 * Architecture fit: Memory manager for the agent context layer.
 */
package com.airural.platform.core.agents.application;

import com.airural.platform.core.agents.domain.AgentMemoryEntity;
import com.airural.platform.core.agents.infrastructure.AgentMemoryRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Service for agent memory persistence and lookup. */
@Service
public class AgentMemoryManager {
    private final AgentMemoryRepository repository;
    private final ObjectMapper objectMapper;

    public AgentMemoryManager(AgentMemoryRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public AgentMemoryEntity remember(String memoryType, String scopeType, UUID scopeId, UUID conversationId, UUID taskId, Object content, Object references) {
        return repository.save(new AgentMemoryEntity(memoryType, scopeType, scopeId, conversationId, taskId, json(content), json(references)));
    }

    @Transactional(readOnly = true)
    public List<AgentMemoryEntity> conversationMemory(UUID conversationId) {
        return conversationId == null ? repository.findAll() : repository.findTop20ByConversationIdOrderByCreatedAtDesc(conversationId);
    }

    private String json(Object value) {
        try { return objectMapper.writeValueAsString(value); } catch (Exception ex) { return "{}"; }
    }
}
