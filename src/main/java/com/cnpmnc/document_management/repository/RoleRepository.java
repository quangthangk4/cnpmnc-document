package com.cnpmnc.document_management.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cnpmnc.document_management.entity.Role;

public interface RoleRepository extends JpaRepository<Role, String> {
    Optional<Role> findByRoleName(String roleName);
}
