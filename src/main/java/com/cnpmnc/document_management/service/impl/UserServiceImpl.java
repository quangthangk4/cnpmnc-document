package com.cnpmnc.document_management.service.impl;

import com.cnpmnc.document_management.dto.request.LoginRequest;
import com.cnpmnc.document_management.dto.request.RegisterRequest;
import com.cnpmnc.document_management.dto.response.TokenResponse;
import com.cnpmnc.document_management.dto.response.UserResponse;
import com.cnpmnc.document_management.entity.Role;
import com.cnpmnc.document_management.entity.User;
import com.cnpmnc.document_management.enums.PurposeToken;
import com.cnpmnc.document_management.exception.AppException;
import com.cnpmnc.document_management.exception.ErrorCode;
import com.cnpmnc.document_management.repository.RoleRepository;
import com.cnpmnc.document_management.repository.UserRepository;
import com.cnpmnc.document_management.service.AuthService;
import com.cnpmnc.document_management.service.UserService;
import com.cnpmnc.document_management.shared.CurrentUserUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthService authService;
    private final RoleRepository roleRepository;

    @Override
    public TokenResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.username()).orElseThrow(
                () -> new AppException(ErrorCode.USERNAME_OR_PASSWORD_INCORRECT)
        );

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new AppException(ErrorCode.USERNAME_OR_PASSWORD_INCORRECT);
        }

        String accessToken = authService.generateToken(user, PurposeToken.ACCESS);
        String refreshToken = authService.generateToken(user, PurposeToken.REFRESH);

        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Override
    public TokenResponse registerUser(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new AppException(ErrorCode.USERNAME_ALREADY_EXISTS);
        }

        Role role = roleRepository.findByRoleName("USER").orElseThrow(
                () -> new IllegalStateException("USER role not found. Ensure RoleDataInitializer runs before UserAccountInitializer.")
        );

        Set<Role> roles = Set.of(role);
        User user = User.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .username(request.username())
                .password(passwordEncoder.encode(request.password()))
                .departmentId(request.departmentId())
                .roles(roles)
                .build();

        userRepository.save(user);
        String accessToken = authService.generateToken(user, PurposeToken.ACCESS);
        String refreshToken = authService.generateToken(user, PurposeToken.REFRESH);

        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Override
    public UserResponse getCurrentUser() {
        String userId = CurrentUserUtils.getUserId();
        User user = userRepository.findById(userId).orElseThrow(
            () -> new AppException(ErrorCode.USER_NOT_FOUND)
        );

        return UserResponse.builder()
            .id(user.getId())
            .firstName(user.getFirstName())
            .lastName(user.getLastName())
            .username(user.getUsername())
            .departmentId(user.getDepartmentId())
            .roles(user.getRoles())
            .build();
    }
}
