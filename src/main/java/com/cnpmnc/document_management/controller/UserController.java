package com.cnpmnc.document_management.controller;

import com.cnpmnc.document_management.dto.ApiResponse;
import com.cnpmnc.document_management.dto.response.TokenResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    @PostMapping("/login")
    public ApiResponse<TokenResponse> login() {

        return ApiResponse.success(null);
    }
}
