package com.cnpmnc.document_management.config;


import com.cnpmnc.document_management.enums.PurposeToken;
import com.cnpmnc.document_management.exception.AppException;
import com.cnpmnc.document_management.properties.RSAKeyRecord;
import com.cnpmnc.document_management.service.IntrospectToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomJwtDecoder implements JwtDecoder {
    private final IntrospectToken introspectToken;
    private final NimbusJwtDecoder nimbusJwtDecoder;
    private final RSAKeyRecord rsaKeyRecord;

    @Override
    public Jwt decode(String token) {
        try {
            introspectToken.verifyToken(token, rsaKeyRecord.rsaPublicKey(), PurposeToken.ACCESS);
        } catch (AppException e) {
            throw new BadJwtException(e.getMessage(), e);
        }
        return nimbusJwtDecoder.decode(token);
    }

}
