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
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
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
    private final S3Client s3Client;
    
    @Value("${aws.s3.bucket}")
    private String bucketName;
    
    @Value("${document.max-file-size:10485760}")
    private Long maxFileSize;
    
    public DocumentResponse uploadDocument(DocumentUploadRequest request, String userId) throws IOException {
        MultipartFile file = request.getFile();
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is required");
        }
        
        if (file.getSize() > maxFileSize) {
            throw new IllegalArgumentException("File size exceeds maximum: " + maxFileSize);
        }
        
        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new IllegalArgumentException("Department not found"));
        
        User user = null;
        if (userId != null) {
            user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
        }
        
        String originalFileName = file.getOriginalFilename();
        String savedFileName = generateUniqueFileName(originalFileName);

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(savedFileName)
                .contentType(file.getContentType())
                .build();

        s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
        
        Document document = Document.builder()
                .title(request.getTitle())
                .type(request.getType())
                .department(department)
                .createdBy(user)
                .filePath(savedFileName)
                .fileName(originalFileName)
                .fileSize(file.getSize())
                .fileType(file.getContentType())
                .build();
        
        Document savedDocument = documentRepository.save(document);
        
        String usernameForLog = user != null ? user.getUsername() : "Anonymous";
        log.info("Document uploaded with ID: {} by user: {}", savedDocument.getId(), usernameForLog);
        
        return convertToResponse(savedDocument);
    }

    @Transactional(readOnly = true)
    public byte[] downloadFileFromS3(String key) {
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            ResponseBytes<GetObjectResponse> objectBytes = s3Client.getObjectAsBytes(getObjectRequest);
            return objectBytes.asByteArray();
        } catch (Exception e) {
            log.error("Error downloading file from S3: {}", e.getMessage());
            throw new RuntimeException("Could not download file from cloud storage");
        }
    }
    
    @Transactional(readOnly = true)
    public DocumentResponse getDocument(Integer documentId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new IllegalArgumentException("Document not found"));
        return convertToResponse(document);
    }

    @Transactional(readOnly = true)
    public Document getDocumentEntity(Integer documentId) {
        return documentRepository.findById(documentId)
                .orElseThrow(() -> new IllegalArgumentException("Document not found"));
    }

    @Transactional(readOnly = true)
    public List<DocumentResponse> getDocumentsByDepartment(Integer departmentId) {
        return documentRepository.findByDepartmentId(departmentId).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DocumentResponse> getDocumentsByType(String type) {
        return documentRepository.findByType(type).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DocumentResponse> getDocumentsByUser(UUID userId) {
        return documentRepository.findByCreatedById(userId).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DocumentResponse> getAllDocuments() {
        return documentRepository.findAll().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public void deleteDocument(Integer documentId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new IllegalArgumentException("Document not found"));
        
        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(document.getFilePath())
                .build();
                
        s3Client.deleteObject(deleteObjectRequest);
        documentRepository.deleteById(documentId);
    }
    
    private String generateUniqueFileName(String originalFileName) {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String extension = "";
        if (originalFileName != null && originalFileName.contains(".")) {
            extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }
        return timestamp + extension;
    }
    
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
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
}