package com.cnpmnc.document_management.service.impl;

import com.cnpmnc.document_management.dto.request.RegisterRequest;
import com.cnpmnc.document_management.dto.response.RegisterResponse;
import com.cnpmnc.document_management.dto.response.TokenResponse;
import com.cnpmnc.document_management.entity.Role;
import com.cnpmnc.document_management.entity.User;
import com.cnpmnc.document_management.exception.AppException;
import com.cnpmnc.document_management.exception.ErrorCode;
import com.cnpmnc.document_management.repository.RoleRepository;
import com.cnpmnc.document_management.repository.UserRepository;
import com.cnpmnc.document_management.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public TokenResponse login() {
        return null;
    }

    @Override
    public RegisterResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new AppException(ErrorCode.USER_ALREADY_EXISTS);
        }

        try {
            // Tìm role từ DB theo tên (vd: "USER", "ADMIN")
            Role role = roleRepository.findByRoleName(request.getRole())
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));

            User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .departmentId(request.getDepartmentId())
                .createdAt(LocalDateTime.now())
                .build();

            user.getRoles().add(role);

            User savedUser = userRepository.save(user);

            // Lấy tên role đầu tiên để trả về response
            String roleName = savedUser.getRoles().stream()
                .map(Role::getRoleName)
                .findFirst()
                .orElse(null);

            return RegisterResponse.builder()
                .id(savedUser.getId())
                .firstName(savedUser.getFirstName())
                .lastName(savedUser.getLastName())
                .username(savedUser.getUsername())
                .departmentId(savedUser.getDepartmentId())
                .role(roleName)
                .createdAt(savedUser.getCreatedAt())
                .build();

        } catch (AppException e) {
            throw e; // re-throw AppException để không bị bọc lại
        } catch (Exception e) {
            throw new AppException(ErrorCode.REGISTER_FAILED);
        }
    }
}