package com.cnpmnc.document_management.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DocumentResponse implements Serializable {
    private Integer id;
    private String title;
    private String type;
    private Integer departmentId;
    private String departmentName;
    private UUID createdBy;
    private String createdByName;
    private String fileName;
    private Long fileSize;
    private String fileType;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
