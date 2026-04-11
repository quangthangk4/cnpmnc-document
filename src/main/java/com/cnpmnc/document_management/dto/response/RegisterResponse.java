package com.cnpmnc.document_management.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterResponse {
    private String id;
    private String firstName;
    private String lastName;
    private String username;
    private Integer departmentId;
    private String role;
    private LocalDateTime createdAt;
}