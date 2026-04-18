package com.cnpmnc.document_management.controller;

import com.cnpmnc.document_management.dto.ApiResponse;
import com.cnpmnc.document_management.dto.response.UserResponse;
import com.cnpmnc.document_management.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;

  @GetMapping("/me")
  @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
  public ApiResponse<UserResponse> getCurrentUser() {
    return ApiResponse.success("success", userService.getCurrentUser());
  }
}
