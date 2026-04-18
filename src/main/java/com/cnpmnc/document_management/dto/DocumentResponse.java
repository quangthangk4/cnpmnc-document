package com.cnpmnc.document_management.dto;

import com.cnpmnc.document_management.entity.DocumentType;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DocumentResponse implements Serializable {
    private Integer id;
    private String title;
    private DocumentType type;
    private Integer departmentId;
    private String departmentName;
    private String createdBy;
    private String createdByName;
    private String fileName;
    private Long fileSize;
    private String fileType;
    private String fileExtension;
    private Integer currentVersion;
    private Integer versionId;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
