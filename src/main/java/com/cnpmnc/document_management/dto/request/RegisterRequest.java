package com.cnpmnc.document_management.dto.request;

import jakarta.validation.constraints.NotBlank;

public record RegisterRequest(
        String firstName,
        String lastName,
        @NotBlank(message = "Username is required")
        String username,
        @NotBlank(message = "Password is required")
        String password,
        Long departmentId
) {
}
