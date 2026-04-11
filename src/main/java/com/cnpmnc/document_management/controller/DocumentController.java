package com.cnpmnc.document_management.controller;

import com.cnpmnc.document_management.dto.ApiResponse;
import com.cnpmnc.document_management.dto.DocumentResponse;
import com.cnpmnc.document_management.dto.DocumentUploadRequest;
import com.cnpmnc.document_management.service.impl.DocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.format.annotation.DateTimeFormat;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
@Tag(name = "Document Management", description = "Upload and manage documents with metadata")
public class DocumentController {
    
    private final DocumentService documentService;
    
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload document", description = "Upload a document with metadata")
    public ApiResponse<DocumentResponse> uploadDocument(
            @RequestParam String title,
            @RequestParam String type,
            @RequestParam Integer departmentId,
            @RequestParam MultipartFile file,
            @RequestParam(required = false) UUID userId) throws IOException {
        
        
        DocumentUploadRequest request = DocumentUploadRequest.builder()
                .title(title)
                .type(type)
                .departmentId(departmentId)
                .file(file)
                .build();
        
        DocumentResponse response = documentService.uploadDocument(request, userId);
        return ApiResponse.created("Document uploaded successfully", response);
    }
    
    @GetMapping("/{documentId}")
    @Operation(summary = "Get document", description = "Retrieve document details by ID")
    public ApiResponse<DocumentResponse> getDocument(@PathVariable Integer documentId) {
        DocumentResponse response = documentService.getDocument(documentId);
        return ApiResponse.success(response);
    }
    
    @GetMapping("/department/{departmentId}")
    @Operation(summary = "Get documents by department", description = "List all documents in a department")
    public ApiResponse<List<DocumentResponse>> getByDepartment(@PathVariable Integer departmentId) {
        List<DocumentResponse> documents = documentService.getDocumentsByDepartment(departmentId);
        return ApiResponse.success(documents);
    }
    
    @GetMapping("/type/{type}")
    @Operation(summary = "Get documents by type", description = "List all documents of a specific type")
    public ApiResponse<List<DocumentResponse>> getByType(@PathVariable String type) {
        List<DocumentResponse> documents = documentService.getDocumentsByType(type);
        return ApiResponse.success(documents);
    }
    
    @GetMapping("/user/{userId}")
    @Operation(summary = "Get user documents", description = "List all documents uploaded by a user")
    public ApiResponse<List<DocumentResponse>> getByUser(@PathVariable UUID userId) {
        List<DocumentResponse> documents = documentService.getDocumentsByUser(userId);
        return ApiResponse.success(documents);
    }
    
    @GetMapping
    @Operation(summary = "Get all documents", description = "List all documents in the system")
    public ApiResponse<List<DocumentResponse>> getAllDocuments() {
        List<DocumentResponse> documents = documentService.getAllDocuments();
        return ApiResponse.success(documents);
    }
    
    @DeleteMapping("/{documentId}")
    @Operation(summary = "Delete document", description = "Delete a document and its file")
    public ApiResponse<Void> deleteDocument(@PathVariable Integer documentId) throws IOException {
        documentService.deleteDocument(documentId);
        return ApiResponse.deleteSuccess("Document deleted successfully");
    }
    @GetMapping("/search")
    @Operation(summary = "Search documents", description = "Search by multiple fields. Returns most recent first.")
    public ApiResponse<List<DocumentResponse>> searchDocuments(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Integer departmentId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        List<DocumentResponse> documents = documentService.searchDocuments(title, type, departmentId, startDate, endDate);
        return ApiResponse.success(documents);
    }

    @GetMapping("/{documentId}/download")
    @Operation(summary = "Download document", description = "Download the actual physical file")
    public ResponseEntity<Resource> downloadDocument(@PathVariable Integer documentId) throws IOException {
        // Lấy thông tin metadata của file từ database
        com.cnpmnc.document_management.entity.Document document = documentService.getDocumentEntity(documentId);
        
        // Tạo file resource từ đường dẫn vật lý
        Path filePath = Paths.get(document.getFilePath());
        Resource resource = new UrlResource(filePath.toUri());

        if (!resource.exists() || !resource.isReadable()) {
            throw new RuntimeException("File not found or cannot be read");
        }

        // Trả về file nhị phân đính kèm
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(document.getFileType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + document.getFileName() + "\"")
                .body(resource);
    }
}