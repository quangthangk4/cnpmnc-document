package com.cnpmnc.document_management.service;

import com.cnpmnc.document_management.enums.PurposeToken;
import com.nimbusds.jwt.JWTClaimsSet;

import java.security.interfaces.RSAPublicKey;

public interface IntrospectToken {
    void verifyToken(String token, RSAPublicKey publicKey, PurposeToken expectedPurpose);
    JWTClaimsSet parseAndVerifyToken(String token, RSAPublicKey publicKey);
}