package com.cnpmnc.document_management.service;


import com.cnpmnc.document_management.dto.request.CreateDepartmentRequest;
import com.cnpmnc.document_management.dto.response.DepartmentResponse;

import java.util.List;

public interface DepartmentService {
    List<DepartmentResponse> getAllDepartments();
    void createDepartment(CreateDepartmentRequest name);
}
