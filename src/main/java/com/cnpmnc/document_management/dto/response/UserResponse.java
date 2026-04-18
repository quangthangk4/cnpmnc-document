package com.cnpmnc.document_management.dto.response;

import com.cnpmnc.document_management.entity.Role;
import java.util.Set;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserResponse {
  private String id;
  private String firstName;
  private String lastName;
  private String username;
  private Integer departmentId;
  private Set<Role> roles;
}
