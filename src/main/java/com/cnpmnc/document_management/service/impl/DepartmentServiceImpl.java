package com.cnpmnc.document_management.service.impl;

import com.cnpmnc.document_management.dto.response.DepartmentResponse;
import com.cnpmnc.document_management.entity.Department;
import com.cnpmnc.document_management.exception.AppException;
import com.cnpmnc.document_management.exception.ErrorCode;
import com.cnpmnc.document_management.mapper.DepartmentMapper;
import com.cnpmnc.document_management.repository.DepartmentRepository;
import com.cnpmnc.document_management.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl implements DepartmentService {
    private final DepartmentRepository departmentRepository;
    private final DepartmentMapper departmentMapper;

    @Override
    public List<DepartmentResponse> getAllDepartments() {
        return departmentRepository.findAll().stream()
                .map(departmentMapper::toDepartmentResponse)
                .toList();
    }

    @Override
    public void createDepartment(String name) {
        if (departmentRepository.existsByName(name)) {
            throw new AppException(ErrorCode.DEPARTMENT_ALREADY_EXISTS);
        }
        Department department = Department.builder()
                .name(name)
                .build();
        departmentRepository.save(department);
    }
}
