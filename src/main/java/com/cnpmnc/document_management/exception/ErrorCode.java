package com.cnpmnc.document_management.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {
    FORBIDDEN(1000, "Access denied", HttpStatus.FORBIDDEN),
    UNAUTHENTICATED(1001, "Authentication required", HttpStatus.UNAUTHORIZED),
    INVALID_TOKEN(1402, "Invalid token", HttpStatus.UNAUTHORIZED),
    TOKEN_EXPIRED(1403, "Token expired", HttpStatus.UNAUTHORIZED),
    TOKEN_GENERATION_FAILED(1404, "Token generation failed", HttpStatus.INTERNAL_SERVER_ERROR),
    NOT_OWNER(1405, "You are not the owner of this resource", HttpStatus.FORBIDDEN),
    TOKEN_REFRESH_FAILED(1406, "Token refresh failed", HttpStatus.BAD_REQUEST),
    TOKEN_INVALID(2604, "Token invalid", HttpStatus.UNAUTHORIZED),
    USER_NOT_FOUND(1100, "User not found", HttpStatus.NOT_FOUND),
    USER_ALREADY_EXISTS(1101, "Username already exists", HttpStatus.CONFLICT),
    INVALID_REGISTER_DATA(1102, "Invalid registration data", HttpStatus.BAD_REQUEST),
    REGISTER_FAILED(1103, "Registration failed", HttpStatus.INTERNAL_SERVER_ERROR),

    ;

    private final int code;
    private final String message;
    private final HttpStatusCode status;

    ErrorCode(int code, String message, HttpStatusCode status) {
        this.code = code;
        this.message = message;
        this.status = status;
    }
}
