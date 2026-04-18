package com.cnpmnc.document_management.controller;

import com.cnpmnc.document_management.dto.ApiResponse;
import com.cnpmnc.document_management.dto.DocumentResponse;
import com.cnpmnc.document_management.dto.DocumentUploadRequest;
import com.cnpmnc.document_management.dto.request.DocumentUpdateRequest;
import com.cnpmnc.document_management.dto.response.DocumentVersionResponse;
import com.cnpmnc.document_management.entity.DocumentType;
import com.cnpmnc.document_management.entity.DocumentVersion;
import com.cnpmnc.document_management.service.impl.DocumentService;
import com.cnpmnc.document_management.shared.CurrentUserUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
@Tag(name = "Document Management", description = "Upload and manage documents with metadata and versioning")
public class DocumentController {
    
    private final DocumentService documentService;
    
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Upload document", description = "Upload a document with metadata (creates Version 1)")
    public ApiResponse<DocumentResponse> uploadDocument(
            @RequestParam String title,
            @RequestParam DocumentType type,
            @RequestParam Integer departmentId,
            @RequestParam MultipartFile file) throws IOException {

        String userId = CurrentUserUtils.getUserId();
        DocumentUploadRequest request = DocumentUploadRequest.builder()
                .title(title).type(type).departmentId(departmentId).file(file).build();
        
        DocumentResponse response = documentService.uploadDocument(request, userId);
        return ApiResponse.created("Document uploaded successfully", response);
    }

    @PutMapping(value = "/{documentId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Update document", description = "Update metadata and optionally upload a new file version")
    public ApiResponse<DocumentResponse> updateDocument(
            @PathVariable Integer documentId,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) DocumentType type,
            @RequestParam(required = false) Integer departmentId,
            @RequestParam(required = false) MultipartFile file) throws IOException {
        
        DocumentUpdateRequest request = DocumentUpdateRequest.builder()
                .title(title).type(type).departmentId(departmentId).file(file).build();

        DocumentResponse response = documentService.updateDocument(documentId, request);
        return ApiResponse.success("Document updated successfully", response);
    }
    
    @GetMapping("/{documentId}")
    @PreAuthorize("hasRole('ADMIN') or @documentService.canView(#documentId)")
    public ApiResponse<DocumentResponse> getDocument(@PathVariable("documentId") Integer documentId) {
        DocumentResponse response = documentService.getDocument(documentId);
        return ApiResponse.success(response);
    }

    @GetMapping("/{documentId}/versions")
    @PreAuthorize("hasRole('ADMIN') or @documentService.canView(#documentId)")
    @Operation(summary = "Get document versions", description = "Retrieve all versions of a document")
    public ApiResponse<List<DocumentVersionResponse>> getDocumentVersions(@PathVariable Integer documentId) {
        List<DocumentVersionResponse> versions = documentService.getDocumentVersions(documentId);
        return ApiResponse.success(versions);
    }

    // --- DOWNLOAD & PREVIEW LATEST ---

    @GetMapping("/{documentId}/download")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Download latest version", description = "Force download the most recent version of a document")
    public ResponseEntity<Resource> downloadLatest(@PathVariable Integer documentId) throws IOException {
        com.cnpmnc.document_management.entity.Document doc = documentService.getDocumentEntity(documentId);
        return buildFileResponse(doc.getFilePath(), doc.getFileName(), doc.getFileExtension(), false);
    }

    @GetMapping("/{documentId}/preview")
//    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Preview latest version", description = "Open the most recent version in browser (if supported)")
    public ResponseEntity<Resource> previewLatest(@PathVariable Integer documentId) throws IOException {
        com.cnpmnc.document_management.entity.Document doc = documentService.getDocumentEntity(documentId);
        return buildFileResponse(doc.getFilePath(), doc.getFileName(), doc.getFileExtension(), true);
    }

    // --- DOWNLOAD & PREVIEW SPECIFIC VERSIONS ---

    @GetMapping("/versions/{versionId}/download")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Download specific version", description = "Force download a specific version from history")
    public ResponseEntity<Resource> downloadVersion(@PathVariable Integer versionId) throws IOException {
        DocumentVersion version = documentService.getVersionEntity(versionId);
        return buildFileResponse(version.getFilePath(), version.getFileName(), version.getFileExtension(), false);
    }

    @GetMapping("/versions/{versionId}/preview")
//    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Preview specific version", description = "Open a specific version in browser (if supported)")
    public ResponseEntity<Resource> previewVersion(@PathVariable Integer versionId) throws IOException {
        DocumentVersion version = documentService.getVersionEntity(versionId);
        return buildFileResponse(version.getFilePath(), version.getFileName(), version.getFileExtension(), true);
    }

    // --- HELPER METHOD ---

    private ResponseEntity<Resource> buildFileResponse(String path, String name, String extension, boolean isPreview) {
        byte[] data = documentService.downloadFileFromS3(path);
        ByteArrayResource resource = new ByteArrayResource(data);
        
        // Sử dụng ContentDisposition để xử lý tên file có ký tự tiếng Việt (UTF-8)
        ContentDisposition contentDisposition = ContentDisposition.builder(isPreview ? "inline" : "attachment")
                .filename(name, StandardCharsets.UTF_8)
                .build();
        
        MediaType mediaType;
        if (isPreview) {
            // Đối với Preview: Cần MediaType chuẩn để trình duyệt có thể hiển thị (ví dụ PDF, Image)
            mediaType = MediaTypeFactory.getMediaType(name)
                    .orElse(MediaTypeFactory.getMediaType("file." + extension)
                    .orElse(MediaType.APPLICATION_OCTET_STREAM));
        } else {
            // Đối với Download: Sử dụng application/octet-stream để ép trình duyệt tải về
            mediaType = MediaType.APPLICATION_OCTET_STREAM;
        }
        
        return ResponseEntity.ok()
            .contentLength(data.length)
            .contentType(mediaType)
            .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString())
            .body(resource);
    }

    // --- OTHER ENDPOINTS ---

    @GetMapping("/department/{departmentId}")
    public ApiResponse<List<DocumentResponse>> getByDepartment(@PathVariable Integer departmentId) {
        return ApiResponse.success(documentService.getDocumentsByDepartment(departmentId));
    }
    
    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ApiResponse<List<DocumentResponse>> getMyDocuments() {
        return ApiResponse.success(documentService.getMyDocuments(CurrentUserUtils.getUserId()));
    }
    
    @DeleteMapping("/{documentId}")
    @PreAuthorize("hasRole('ADMIN') or (@documentService.isOwner(#documentId))")
    public ApiResponse<Void> deleteDocument(@PathVariable Integer documentId) throws IOException {
        documentService.deleteDocument(documentId);
        return ApiResponse.deleteSuccess("Document deleted successfully");
    }

    @GetMapping("/search")
    public ApiResponse<List<DocumentResponse>> searchDocuments(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) DocumentType type,
            @RequestParam(required = false) Integer departmentId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return ApiResponse.success(documentService.searchDocuments(title, type, departmentId, startDate, endDate));
    }
}
