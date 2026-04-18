package com.cnpmnc.document_management.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DocumentUpdateRequest {
    private String title;
    private String type;
    private Integer departmentId;
    private MultipartFile file;
}
