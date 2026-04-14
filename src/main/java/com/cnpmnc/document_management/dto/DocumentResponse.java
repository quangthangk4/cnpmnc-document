package com.cnpmnc.document_management.dto;

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
    private Long id;
    private String title;
    private String type;
    private Long departmentId;
    private String departmentName;
    private String createdBy;
    private String createdByName;
    private String fileName;
    private Long fileSize;
    private String fileType;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
