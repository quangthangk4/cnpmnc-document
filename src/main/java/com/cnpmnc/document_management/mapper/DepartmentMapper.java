package com.cnpmnc.document_management.mapper;

import com.cnpmnc.document_management.dto.response.DepartmentResponse;
import com.cnpmnc.document_management.entity.Department;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DepartmentMapper {
    DepartmentResponse toDepartmentResponse(Department department);
}
