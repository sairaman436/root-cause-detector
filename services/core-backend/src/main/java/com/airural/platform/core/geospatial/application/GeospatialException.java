/*
 * Purpose: Represents domain and application errors from the Geospatial module.
 * Why it exists: Controllers need stable error codes and HTTP statuses without leaking persistence exceptions.
 * Architecture fit: Module-specific exception type handled by the shared API error mapper.
 */
package com.airural.platform.core.geospatial.application;

import org.springframework.http.HttpStatus;

/** Runtime exception carrying a geospatial error code and HTTP status. */
public class GeospatialException extends RuntimeException {
    private final String code;
    private final HttpStatus status;

    public GeospatialException(String code, String message, HttpStatus status) {
        super(message);
        this.code = code;
        this.status = status;
    }

    public String code() { return code; }
    public HttpStatus status() { return status; }
}
