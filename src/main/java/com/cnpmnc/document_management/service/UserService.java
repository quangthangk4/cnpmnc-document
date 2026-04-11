package com.cnpmnc.document_management.service;

import com.cnpmnc.document_management.dto.request.LoginRequest;
import com.cnpmnc.document_management.dto.request.RegisterRequest;
import com.cnpmnc.document_management.dto.response.TokenResponse;

public interface UserService {
    TokenResponse login(LoginRequest request);
    TokenResponse registerUser(RegisterRequest user);
}
