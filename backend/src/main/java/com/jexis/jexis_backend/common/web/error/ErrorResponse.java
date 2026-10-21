package com.jexis.jexis_backend.common.web.error;

/**
 * Strict returned error shape
 * <p>
 * This class defines the structure of error responses sent back to clients when
 * exceptions occur. It includes fields for an error code, a human-readable
 * message, and an HTTP status
 * <p>
 * Author: Leo
 */
public class ErrorResponse {
    String code;
    String message;
    Integer status;

    public ErrorResponse(Integer status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

    public Integer getStatus() {
        return status;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
