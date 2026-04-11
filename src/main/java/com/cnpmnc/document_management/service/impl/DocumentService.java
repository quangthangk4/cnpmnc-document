package com.cnpmnc.document_management.service.impl;

import com.cnpmnc.document_management.dto.DocumentResponse;
import com.cnpmnc.document_management.dto.DocumentUploadRequest;
import com.cnpmnc.document_management.entity.Department;
import com.cnpmnc.document_management.entity.Document;
import com.cnpmnc.document_management.entity.User;
import com.cnpmnc.document_management.repository.DepartmentRepository;
import com.cnpmnc.document_management.repository.DocumentRepository;
import com.cnpmnc.document_management.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class DocumentService {
    
    private final DocumentRepository documentRepository;
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    
    @Value("${document.upload.dir:uploads}")
    private String uploadDir;
    
    @Value("${document.max-file-size:10485760}")
    private Long maxFileSize;
    
    /**
     * Upload document with metadata to database and file to storage
     */
    public DocumentResponse uploadDocument(DocumentUploadRequest request, UUID userId) throws IOException {
        // Validate file
        MultipartFile file = request.getFile();
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is required");
        }
        
        if (file.getSize() > maxFileSize) {
            throw new IllegalArgumentException("File size exceeds maximum: " + maxFileSize);
        }
        
        // Verify department exists
        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new IllegalArgumentException("Department not found"));
        
        // Cập nhật: Cho phép user null (Ẩn danh)
        User user = null;
        if (userId != null) {
            user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
        }
        
        // Create upload directory
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        // Save file with unique name
        String originalFileName = file.getOriginalFilename();
        String savedFileName = generateUniqueFileName(originalFileName);
        Path filePath = uploadPath.resolve(savedFileName);
        Files.write(filePath, file.getBytes());
        
        // Save document metadata to database
        Document document = Document.builder()
                .title(request.getTitle())
                .type(request.getType())
                .department(department)
                .createdBy(user) // Sẽ là null nếu không có userId
                .filePath(filePath.toString())
                .fileName(originalFileName)
                .fileSize(file.getSize())
                .fileType(file.getContentType())
                .build();
        
        Document savedDocument = documentRepository.save(document);
        
        String usernameForLog = user != null ? user.getUsername() : "Anonymous";
        log.info("Document uploaded with ID: {} by user: {}", savedDocument.getId(), usernameForLog);
        
        return convertToResponse(savedDocument);
    }
    
    /**
     * Get document by ID
     */
    @Transactional(readOnly = true)
    public DocumentResponse getDocument(Integer documentId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new IllegalArgumentException("Document not found"));
        return convertToResponse(document);
    }
    
    /**
     * Get documents by department
     */
    @Transactional(readOnly = true)
    public List<DocumentResponse> getDocumentsByDepartment(Integer departmentId) {
        return documentRepository.findByDepartmentId(departmentId).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Get documents by type
     */
    @Transactional(readOnly = true)
    public List<DocumentResponse> getDocumentsByType(String type) {
        return documentRepository.findByType(type).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Get all documents uploaded by a user
     */
    @Transactional(readOnly = true)
    public List<DocumentResponse> getDocumentsByUser(UUID userId) {
        return documentRepository.findByCreatedById(userId).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Get all documents
     */
    @Transactional(readOnly = true)
    public List<DocumentResponse> getAllDocuments() {
        return documentRepository.findAll().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Delete document and its file
     */
    public void deleteDocument(Integer documentId) throws IOException {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new IllegalArgumentException("Document not found"));
        
        Path filePath = Paths.get(document.getFilePath());
        if (Files.exists(filePath)) {
            Files.delete(filePath);
            log.info("File deleted: {}", filePath);
        }
        
        documentRepository.deleteById(documentId);
        log.info("Document deleted: {}", documentId);
    }
    
    private String generateUniqueFileName(String originalFileName) {
        String timestamp = System.currentTimeMillis() + "";
        String extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        return timestamp + extension;
    }
    
    // Cập nhật: Tránh NullPointerException khi người tạo là null
    private DocumentResponse convertToResponse(Document doc) {
        return DocumentResponse.builder()
                .id(doc.getId())
                .title(doc.getTitle())
                .type(doc.getType())
                .departmentId(doc.getDepartment().getId())
                .departmentName(doc.getDepartment().getName())
                .createdBy(doc.getCreatedBy() != null ? doc.getCreatedBy().getId() : null)
                .createdByName(doc.getCreatedBy() != null ? doc.getCreatedBy().getUsername() : "Anonymous")
                .fileName(doc.getFileName())
                .fileSize(doc.getFileSize())
                .fileType(doc.getFileType())
                .createdAt(doc.getCreatedAt())
                .build();
    }
    @Transactional(readOnly = true)
    public List<DocumentResponse> searchDocuments(String title, String type, Integer departmentId, 
                                                  LocalDateTime startDate, LocalDateTime endDate) {
        return documentRepository.searchDocuments(title, type, departmentId, startDate, endDate)
                .stream()
                .map(this::convertToResponse) // Hàm này bạn đã có sẵn
                .collect(Collectors.toList());
    }

    /**
     * Helper method to get the raw Document entity for downloading
     */
    @Transactional(readOnly = true)
    public Document getDocumentEntity(Integer documentId) {
        return documentRepository.findById(documentId)
                .orElseThrow(() -> new IllegalArgumentException("Document not found"));
    }
}