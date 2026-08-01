/*
 * Purpose: Coordinates agent chat, execution, memory, history, tools, and feedback.
 * Why it exists: The platform needs one central orchestrator that plans, routes, executes, aggregates, evaluates, and audits agent work.
 * Architecture fit: Application facade for the Enterprise Multi-Agent Intelligence Platform.
 */
package com.airural.platform.core.agents.application;

import com.airural.platform.core.agents.domain.*;
import com.airural.platform.core.agents.infrastructure.*;
import com.airural.platform.core.agents.web.dto.AgentDtos.*;
import com.airural.platform.core.ai.application.AiSafetyService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.*;
import java.util.*;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Central orchestrator for multi-agent workflows. */
@Service
public class AgentOrchestratorService {
    private final AgentRepository agentRepository;
    private final AgentConversationRepository conversationRepository;
    private final AgentExecutionRepository executionRepository;
    private final AgentTaskRepository taskRepository;
    private final TaskPlanRepository planRepository;
    private final TaskExecutionRepository taskExecutionRepository;
    private final ReasoningTraceRepository traceRepository;
    private final AgentFeedbackRepository feedbackRepository;
    private final AgentAuditRepository auditRepository;
    private final AgentToolRegistry toolRegistry;
    private final AgentMemoryManager memoryManager;
    private final TaskPlanner planner;
    private final AgentExecutionEngine executionEngine;
    private final AgentResultAggregator aggregator;
    private final AgentEvaluationEngine evaluator;
    private final AiSafetyService safetyService;
    private final ObjectMapper objectMapper;

    public AgentOrchestratorService(AgentRepository agentRepository, AgentConversationRepository conversationRepository, AgentExecutionRepository executionRepository, AgentTaskRepository taskRepository, TaskPlanRepository planRepository, TaskExecutionRepository taskExecutionRepository, ReasoningTraceRepository traceRepository, AgentFeedbackRepository feedbackRepository, AgentAuditRepository auditRepository, AgentToolRegistry toolRegistry, AgentMemoryManager memoryManager, TaskPlanner planner, AgentExecutionEngine executionEngine, AgentResultAggregator aggregator, AgentEvaluationEngine evaluator, AiSafetyService safetyService, ObjectMapper objectMapper) {
        this.agentRepository = agentRepository;
        this.conversationRepository = conversationRepository;
        this.executionRepository = executionRepository;
        this.taskRepository = taskRepository;
        this.planRepository = planRepository;
        this.taskExecutionRepository = taskExecutionRepository;
        this.traceRepository = traceRepository;
        this.feedbackRepository = feedbackRepository;
        this.auditRepository = auditRepository;
        this.toolRegistry = toolRegistry;
        this.memoryManager = memoryManager;
        this.planner = planner;
        this.executionEngine = executionEngine;
        this.aggregator = aggregator;
        this.evaluator = evaluator;
        this.safetyService = safetyService;
        this.objectMapper = objectMapper;
    }

    /** Runs a conversational agent workflow. */
    @Transactional
    public AgentExecutionResponse chat(AgentChatRequest request, UUID userId) {
        AgentConversationEntity conversation = request.conversationId() == null
                ? conversationRepository.save(new AgentConversationEntity(userId, title(request.message()), json(request.context() == null ? Map.of() : request.context())))
                : conversationRepository.findById(request.conversationId()).orElseThrow(() -> new AgentException("CONVERSATION_NOT_FOUND", "Conversation was not found", HttpStatus.NOT_FOUND));
        return executeInternal(request.message(), null, conversation.id(), userId, request.context(), Boolean.FALSE, Boolean.TRUE.equals(request.requireHumanApproval()));
    }

    /** Runs an explicit objective through the orchestrator. */
    @Transactional
    public AgentExecutionResponse execute(AgentExecuteRequest request, UUID userId) {
        return executeInternal(request.objective(), request.preferredAgents(), null, userId, request.context(), Boolean.TRUE.equals(request.parallel()), Boolean.TRUE.equals(request.requireHumanApproval()));
    }

    @Transactional(readOnly = true)
    public Page<AgentResponse> agents(Pageable pageable) {
        return agentRepository.findAll(pageable).map(this::agentResponse);
    }

    @Transactional(readOnly = true)
    public Page<AgentTaskResponse> tasks(Pageable pageable) {
        return taskRepository.findAll(pageable).map(task -> new AgentTaskResponse(task.id(), task.agentKey(), task.taskType(), task.status(), task.resultJson()));
    }

    @Transactional(readOnly = true)
    public List<AgentMemoryResponse> memory(UUID conversationId) {
        return memoryManager.conversationMemory(conversationId).stream()
                .map(memory -> new AgentMemoryResponse(memory.id(), memory.memoryType(), memory.scopeType(), memory.conversationId(), memory.contentJson(), memory.createdAt()))
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<AgentHistoryResponse> history(Pageable pageable) {
        return executionRepository.findAll(pageable)
                .map(execution -> new AgentHistoryResponse(execution.id(), execution.conversationId(), execution.status(), execution.confidence(), execution.requiresApproval(), execution.latencyMs(), execution.createdAt()));
    }

    @Transactional
    public AgentFeedbackResponse feedback(AgentFeedbackRequest request, UUID userId) {
        if (!executionRepository.existsById(request.executionId())) {
            throw new AgentException("AGENT_EXECUTION_NOT_FOUND", "Agent execution was not found", HttpStatus.NOT_FOUND);
        }
        AgentFeedbackEntity feedback = feedbackRepository.save(new AgentFeedbackEntity(request.executionId(), userId, request.rating(), request.comment(), request.approvalDecision() == null ? "REVIEWED" : request.approvalDecision()));
        auditRepository.save(new AgentAuditEntity(userId, "AGENT_FEEDBACK_RECORDED", "AGENT_EXECUTION", request.executionId(), "SUCCESS", json(Map.of("rating", request.rating(), "approvalDecision", feedback.approvalDecision()))));
        return new AgentFeedbackResponse(feedback.id(), feedback.executionId(), feedback.rating(), feedback.approvalDecision());
    }

    @Transactional(readOnly = true)
    public Page<ToolResponse> tools(Pageable pageable) {
        List<ToolResponse> tools = toolRegistry.definitions().stream()
                .map(tool -> new ToolResponse(tool.id(), tool.toolKey(), tool.name(), tool.category(), tool.description(), tool.healthStatus(), tool.metadataJson()))
                .toList();
        int start = Math.min((int) pageable.getOffset(), tools.size());
        int end = Math.min(start + pageable.getPageSize(), tools.size());
        return new PageImpl<>(tools.subList(start, end), pageable, tools.size());
    }

    private AgentExecutionResponse executeInternal(String objective, List<String> preferredAgents, UUID conversationId, UUID userId, Map<String, Object> context, boolean parallel, boolean requestedApproval) {
        Instant started = Instant.now();
        String safeObjective = safetyService.validateAndMask(objective);
        List<String> selectedAgents = planner.planAgents(safeObjective, preferredAgents);
        String planJson = planner.planJson(safeObjective, selectedAgents, parallel);
        TaskPlanEntity plan = planRepository.save(new TaskPlanEntity(conversationId, safeObjective, planJson, userId));
        TaskExecutionEntity taskExecution = taskExecutionRepository.save(new TaskExecutionEntity(plan.id()));
        AgentExecutionEntity execution = executionRepository.save(new AgentExecutionEntity(conversationId, plan.id(), json(Map.of("objective", safeObjective, "context", context == null ? Map.of() : context))));

        List<AgentTaskEntity> tasks = new ArrayList<>();
        try {
            int priority = 1;
            for (String agentKey : selectedAgents) {
                ensureAgentExists(agentKey);
                tasks.add(executionEngine.executeTask(execution.id(), agentKey, safeObjective, priority++));
            }
            List<ReasoningTraceEntity> traces = traceRepository.findByExecutionIdOrderByIdAsc(execution.id());
            AgentResultAggregator.AggregatedResult result = aggregator.aggregate(tasks, traces);
            boolean consequential = selectedAgents.stream().anyMatch(agent -> agent.contains("root-cause") || agent.contains("recommendation"));
            boolean approval = requestedApproval || evaluator.humanApprovalRequired(consequential, result.confidence(), result.citations());
            long latency = Duration.between(started, Instant.now()).toMillis();
            execution.complete(json(Map.of("response", result.response(), "citations", result.citations())), result.confidence(), latency, approval);
            taskExecution.complete();
            memoryManager.remember("CONVERSATION_MEMORY", "CONVERSATION", conversationId, conversationId, null, Map.of("objective", safeObjective, "response", result.response()), result.citations());
            auditRepository.save(new AgentAuditEntity(userId, "AGENT_EXECUTION_COMPLETED", "AGENT_EXECUTION", execution.id(), "SUCCESS", json(Map.of("agents", selectedAgents, "requiresApproval", approval))));
            return response(executionRepository.save(execution), tasks, traces, result.response(), result.citations(), approval);
        } catch (Exception ex) {
            long latency = Duration.between(started, Instant.now()).toMillis();
            execution.fail(json(Map.of("error", ex.getMessage())), latency);
            taskExecution.fail(ex.getMessage());
            auditRepository.save(new AgentAuditEntity(userId, "AGENT_EXECUTION_FAILED", "AGENT_EXECUTION", execution.id(), "FAILURE", json(Map.of("error", ex.getMessage()))));
            throw ex;
        }
    }

    private void ensureAgentExists(String agentKey) {
        agentRepository.findByAgentKey(agentKey).orElseThrow(() -> new AgentException("AGENT_NOT_FOUND", "Agent is not registered: " + agentKey, HttpStatus.NOT_FOUND));
    }

    private AgentExecutionResponse response(AgentExecutionEntity execution, List<AgentTaskEntity> tasks, List<ReasoningTraceEntity> traces, String response, List<Citation> citations, boolean approval) {
        return new AgentExecutionResponse(
                execution.id(),
                execution.conversationId(),
                execution.planId(),
                execution.status(),
                response,
                tasks.stream().map(task -> new AgentTaskResponse(task.id(), task.agentKey(), task.taskType(), task.status(), task.resultJson())).toList(),
                traces.stream().map(trace -> new ReasoningTraceResponse(trace.id(), trace.agentKey(), trace.content(), trace.confidence())).toList(),
                citations,
                execution.confidence(),
                approval,
                execution.latencyMs());
    }

    private AgentResponse agentResponse(AgentEntity agent) {
        return new AgentResponse(agent.id(), agent.agentKey(), agent.name(), agent.agentType(), agent.status(), agent.description(), list(agent.capabilitiesJson()));
    }

    private String title(String message) {
        return message.length() <= 80 ? message : message.substring(0, 80);
    }

    private String json(Object value) {
        try { return objectMapper.writeValueAsString(value); } catch (Exception ex) { return "{}"; }
    }

    private List<String> list(String json) {
        try { return objectMapper.readValue(json, new TypeReference<List<String>>() {}); } catch (Exception ex) { return List.of(); }
    }
}
