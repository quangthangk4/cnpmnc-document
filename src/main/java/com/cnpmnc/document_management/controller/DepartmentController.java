package com.cnpmnc.document_management.controller;

import com.cnpmnc.document_management.dto.ApiResponse;
import com.cnpmnc.document_management.dto.response.DepartmentResponse;
import com.cnpmnc.document_management.service.DepartmentService;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
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
    public ApiResponse<Void> createDepartment(@RequestBody
                                                                @NotBlank(message = "department name is required")
                                                                String name) {
        departmentService.createDepartment(name);
        return ApiResponse.success("success");
    }
}
