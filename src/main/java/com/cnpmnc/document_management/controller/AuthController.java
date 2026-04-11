package com.cnpmnc.document_management.controller;

import com.cnpmnc.document_management.dto.ApiResponse;
import com.cnpmnc.document_management.dto.request.LoginRequest;
import com.cnpmnc.document_management.dto.request.RegisterRequest;
import com.cnpmnc.document_management.dto.response.TokenResponse;
import com.cnpmnc.document_management.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;


    @PostMapping("/login")
    public ApiResponse<TokenResponse> login(@RequestBody @Valid LoginRequest loginRequest) {
        return ApiResponse.success("login success", userService.login(loginRequest));
    }

    @PostMapping("/register")
    public ApiResponse<TokenResponse> register(@RequestBody @Valid RegisterRequest request) {
        userService.registerUser(request);
        return ApiResponse.success("register success");
    }
}
