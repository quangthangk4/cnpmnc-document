package com.cnpmnc.document_management.service.impl;

import com.cnpmnc.document_management.entity.Role;
import com.cnpmnc.document_management.entity.User;
import com.cnpmnc.document_management.enums.PurposeToken;
import com.cnpmnc.document_management.enums.StaticVariable;
import com.cnpmnc.document_management.exception.AppException;
import com.cnpmnc.document_management.exception.ErrorCode;
import com.cnpmnc.document_management.properties.RSAKeyRecord;
import com.cnpmnc.document_management.service.AuthService;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final RSAKeyRecord rsaKeyRecord;

    @Override
    public String generateToken(User user, PurposeToken purpose) {
        // Header
        JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.RS256)
                .keyID(rsaKeyRecord.keyId())
                .type(JOSEObjectType.JWT)
                .build();
        // Payload
        long expireSeconds = 86400;
        String jwtId = UUID.randomUUID().toString();

        JWTClaimsSet claimsSet = buildClaims(user, purpose, jwtId, expireSeconds);
        Payload payload = new Payload(claimsSet.toJSONObject());

        JWSObject object = new JWSObject(header, payload);
        // Signature
        try {
            if (rsaKeyRecord.rsaPrivateKey() == null) {
                throw new IllegalStateException("Private key is not initialized");
            }
            JWSSigner signer = new RSASSASigner(rsaKeyRecord.rsaPrivateKey());
            object.sign(signer);
        } catch (JOSEException e) {
            log.error("Failed to sign JWT", e);
            throw new AppException(ErrorCode.TOKEN_GENERATION_FAILED);
        }
        return object.serialize();
    }

    private JWTClaimsSet buildClaims(User user, PurposeToken purpose, String jwtId, long expireSeconds) {
        return new JWTClaimsSet.Builder()
                .issuer("document.com")
                .subject(user.getId().toString())
                .issueTime(Date.from(Instant.now()))
                .expirationTime(Date.from(Instant.now().plusSeconds(expireSeconds)))
                .jwtID(jwtId)
                .claim("scope", buildScopes(user.getRoles()))
                .claim(StaticVariable.PURPOSE, purpose)
                .audience("document.com")
                .build();
    }

    private List<String> buildScopes(Set<Role> roles) {
        return roles.stream().map(Role::getRoleName).toList();
    }

}
