package com.cnpmnc.document_management.service;

import com.cnpmnc.document_management.entity.User;
import com.cnpmnc.document_management.enums.PurposeToken;

public interface AuthService {
    String generateToken(User user, PurposeToken purpose);
}
