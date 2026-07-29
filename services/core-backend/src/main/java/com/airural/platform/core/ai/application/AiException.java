/*
 * Purpose: Represents AI foundation application errors.
 * Why it exists: AI gateway, prompt, model, embedding, and RAG workflows need consistent domain errors.
 * Architecture fit: Application-layer exception mapped by the global REST error handler.
 */
package com.airural.platform.core.ai.application;

import org.springframework.http.HttpStatus;

/** Runtime exception for AI foundation failures. */
public class AiException extends RuntimeException {
    private final String code;
    private final HttpStatus status;

    public AiException(String code, String message, HttpStatus status) {
        super(message);
        this.code = code;
        this.status = status;
    }

    public String code() { return code; }
    public HttpStatus status() { return status; }
}
