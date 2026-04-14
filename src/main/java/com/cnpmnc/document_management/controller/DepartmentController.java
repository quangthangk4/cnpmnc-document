package com.cnpmnc.document_management.controller;

import com.cnpmnc.document_management.dto.ApiResponse;
import com.cnpmnc.document_management.dto.request.CreateDepartmentRequest;
import com.cnpmnc.document_management.dto.response.DepartmentResponse;
import com.cnpmnc.document_management.service.DepartmentService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;

    @GetMapping
    public ApiResponse<List<DepartmentResponse>> getAllDepartments() {
        return ApiResponse.success("success", departmentService.getAllDepartments());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> createDepartment(@RequestBody @Valid CreateDepartmentRequest request) {
        departmentService.createDepartment(request);
        return ApiResponse.success("success");
    }
}
