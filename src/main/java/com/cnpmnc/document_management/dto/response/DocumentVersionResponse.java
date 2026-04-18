package com.cnpmnc.document_management.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DocumentVersionResponse {
    private Integer id;
    private Integer versionNumber;
    private String fileName;
    private Long fileSize;
    private String fileType;
    private String uploadBy;
    private String uploadByName;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
