/*
 * Purpose: Represents multi-agent platform application errors.
 * Why it exists: Agent orchestration, planning, memory, tool invocation, and feedback APIs need consistent domain failures.
 * Architecture fit: Application exception mapped by the existing global REST error handler.
 */
package com.airural.platform.core.agents.application;

import org.springframework.http.HttpStatus;

/** Runtime exception for agent platform failures. */
public class AgentException extends RuntimeException {
    private final String code;
    private final HttpStatus status;

    public AgentException(String code, String message, HttpStatus status) {
        super(message);
        this.code = code;
        this.status = status;
    }

    public String code() { return code; }
    public HttpStatus status() { return status; }
}
