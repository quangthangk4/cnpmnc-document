package com.cnpmnc.document_management.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateDepartmentRequest {

  @NotBlank(message = "department name is required")
  private String name;
}