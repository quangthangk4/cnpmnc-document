package com.cnpmnc.document_management.controller;

import com.cnpmnc.document_management.dto.ApiResponse;
import com.cnpmnc.document_management.dto.response.TokenResponse;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.cnpmnc.document_management.dto.request.RegisterRequest;
import com.cnpmnc.document_management.dto.response.RegisterResponse;
import com.cnpmnc.document_management.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor 
public class UserController {
    private final UserService userService;

    @PostMapping("/login")
    public ApiResponse<TokenResponse> login() {

        return ApiResponse.success(null);
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        RegisterResponse response = userService.register(request);
        return ApiResponse.created("User registered successfully", response);
    }
}
