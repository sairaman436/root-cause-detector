/*
 * Purpose: Enforces AI safety checks before gateway, embedding, and RAG processing.
 * Why it exists: Prompt injection, sensitive data leakage, and oversize prompts must be blocked consistently.
 * Architecture fit: AI safety layer used by all Milestone 8 AI workflows.
 */
package com.airural.platform.core.ai.application;

import java.util.*;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

/** Validates prompts and masks sensitive data. */
@Service
public class AiSafetyService {
    private static final List<String> INJECTION_MARKERS = List.of("ignore previous instructions", "system prompt", "developer message", "jailbreak", "bypass policy");
    private static final Pattern EMAIL = Pattern.compile("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}");
    private static final Pattern PHONE = Pattern.compile("\\b(?:\\+?\\d[\\d -]{7,}\\d)\\b");
    private final int maxPromptChars;

    public AiSafetyService(@Value("${airural.ai.safety.max-prompt-chars:12000}") int maxPromptChars) {
        this.maxPromptChars = maxPromptChars;
    }

    /** Validates and masks a prompt before model or embedding use. */
    public String validateAndMask(String text) {
        if (text == null || text.isBlank()) {
            throw new AiException("AI_PROMPT_EMPTY", "Prompt text is required", HttpStatus.BAD_REQUEST);
        }
        if (text.length() > maxPromptChars) {
            throw new AiException("AI_PROMPT_TOO_LARGE", "Prompt exceeds configured AI safety limit", HttpStatus.BAD_REQUEST);
        }
        String lower = text.toLowerCase(Locale.ROOT);
        if (INJECTION_MARKERS.stream().anyMatch(lower::contains)) {
            throw new AiException("PROMPT_INJECTION_DETECTED", "Prompt was blocked by AI safety validation", HttpStatus.BAD_REQUEST);
        }
        return maskSensitiveData(text);
    }

    /** Masks sensitive values in model output before durable storage or downstream reuse. */
    public String maskSensitiveData(String text) {
        if (text == null || text.isBlank()) {
            return text;
        }
        return PHONE.matcher(EMAIL.matcher(text).replaceAll("[REDACTED_EMAIL]")).replaceAll("[REDACTED_PHONE]");
    }
}
