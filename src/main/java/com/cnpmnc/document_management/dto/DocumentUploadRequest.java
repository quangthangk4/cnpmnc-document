package com.cnpmnc.document_management.dto;

import com.cnpmnc.document_management.entity.DocumentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DocumentUploadRequest implements Serializable {
    private String title;
    private DocumentType type;
    private Integer departmentId;
    private MultipartFile file;
}
