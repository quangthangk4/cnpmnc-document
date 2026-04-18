package com.cnpmnc.document_management.service.impl;

import com.cnpmnc.document_management.dto.DocumentResponse;
import com.cnpmnc.document_management.dto.DocumentUploadRequest;
import com.cnpmnc.document_management.dto.request.DocumentUpdateRequest;
import com.cnpmnc.document_management.dto.response.DocumentVersionResponse;
import com.cnpmnc.document_management.entity.Department;
import com.cnpmnc.document_management.entity.Document;
import com.cnpmnc.document_management.entity.DocumentVersion;
import com.cnpmnc.document_management.entity.User;
import com.cnpmnc.document_management.exception.AppException;
import com.cnpmnc.document_management.exception.ErrorCode;
import com.cnpmnc.document_management.repository.DepartmentRepository;
import com.cnpmnc.document_management.repository.DocumentRepository;
import com.cnpmnc.document_management.repository.DocumentVersionRepository;
import com.cnpmnc.document_management.repository.UserRepository;
import com.cnpmnc.document_management.shared.CurrentUserUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class DocumentService {
    
    private final DocumentRepository documentRepository;
    private final DocumentVersionRepository versionRepository;
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final S3Client s3Client;
    
    @Value("${aws.s3.bucket}")
    private String bucketName;
    
    @Value("${document.max-file-size:104857600}")
    private Long maxFileSize;
    
    public DocumentResponse uploadDocument(DocumentUploadRequest request, String userId) throws IOException {
        MultipartFile file = request.getFile();
        validateFile(file);
        
        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new AppException(ErrorCode.DEPARTMENT_NOT_FOUND));
        
        User user = null;
        if (userId != null) {
            user = userRepository.findById(userId)
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        }
        
        String savedFileName = generateUniqueFileName(file.getOriginalFilename());
        uploadFileToS3(savedFileName, file);
        
        Document document = Document.builder()
                .title(request.getTitle())
                .type(request.getType())
                .department(department)
                .createdBy(user)
                .filePath(savedFileName)
                .fileName(file.getOriginalFilename())
                .fileSize(file.getSize())
                .fileType(file.getContentType())
                .currentVersion(1)
                .build();
        
        Document savedDoc = documentRepository.save(document);
        DocumentVersion version = createVersionEntity(savedDoc, 1, savedFileName, file, user);
        versionRepository.save(version);
        
        return convertToResponse(savedDoc);
    }

    public DocumentResponse updateDocument(Integer documentId, DocumentUpdateRequest request) throws IOException {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new AppException(ErrorCode.DOCUMENT_NOT_FOUND));
        
        if (!isAdmin() && !isOwner(documentId)) {
            throw new AppException(ErrorCode.FORBIDDEN);
        }

        if (request.getTitle() != null) document.setTitle(request.getTitle());
        if (request.getType() != null) document.setType(request.getType());
        if (request.getDepartmentId() != null) {
            Department department = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new AppException(ErrorCode.DEPARTMENT_NOT_FOUND));
            document.setDepartment(department);
        }

        MultipartFile file = request.getFile();
        if (file != null && !file.isEmpty()) {
            validateFile(file);
            String userId = CurrentUserUtils.getUserId();
            User user = userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

            String savedFileName = generateUniqueFileName(file.getOriginalFilename());
            uploadFileToS3(savedFileName, file);

            int newVersionNumber = document.getCurrentVersion() + 1;
            DocumentVersion version = createVersionEntity(document, newVersionNumber, savedFileName, file, user);
            versionRepository.save(version);

            document.setCurrentVersion(newVersionNumber);
            document.setFilePath(savedFileName);
            document.setFileName(file.getOriginalFilename());
            document.setFileSize(file.getSize());
            document.setFileType(file.getContentType());
        }
        
        return convertToResponse(documentRepository.save(document));
    }

    /**
     * Lấy thông tin file của một version cụ thể
     */
    @Transactional(readOnly = true)
    public DocumentVersion getVersionEntity(Integer versionId) {
        DocumentVersion version = versionRepository.findById(versionId)
                .orElseThrow(() -> new AppException(ErrorCode.DOCUMENT_NOT_FOUND));
        
        // Kiểm tra quyền xem của tài liệu cha
        if (!canView(version.getDocument().getId())) {
            throw new AppException(ErrorCode.FORBIDDEN);
        }
        return version;
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) throw new AppException(ErrorCode.INVALID_FILE);
        if (file.getSize() > maxFileSize) throw new AppException(ErrorCode.FILE_SIZE_EXCEEDS);
    }

    private void uploadFileToS3(String key, MultipartFile file) throws IOException {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(file.getContentType())
                .build();
        s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
    }

    private DocumentVersion createVersionEntity(Document doc, int versionNum, String path, MultipartFile file, User user) {
        return DocumentVersion.builder()
                .document(doc).versionNumber(versionNum).filePath(path)
                .fileName(file.getOriginalFilename()).fileSize(file.getSize())
                .fileType(file.getContentType()).uploadBy(user).build();
    }

    @Transactional(readOnly = true)
    public byte[] downloadFileFromS3(String key) {
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder().bucket(bucketName).key(key).build();
            ResponseBytes<GetObjectResponse> objectBytes = s3Client.getObjectAsBytes(getObjectRequest);
            return objectBytes.asByteArray();
        } catch (Exception e) {
            log.error("S3 Download Error: {}", e.getMessage());
            throw new RuntimeException("Could not download file from S3");
        }
    }

    @Transactional(readOnly = true)
    public Document getDocumentEntity(Integer documentId) {
        if (!canView(documentId)) throw new AppException(ErrorCode.FORBIDDEN);
        return documentRepository.findById(documentId).orElseThrow(() -> new AppException(ErrorCode.DOCUMENT_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public DocumentResponse getDocument(Integer documentId) {
        return convertToResponse(getDocumentEntity(documentId));
    }

    public void deleteDocument(Integer documentId) {
        Document document = documentRepository.findById(documentId).orElseThrow(() -> new AppException(ErrorCode.DOCUMENT_NOT_FOUND));
        if (!isAdmin() && !isOwner(documentId)) throw new AppException(ErrorCode.FORBIDDEN);
        s3Client.deleteObject(DeleteObjectRequest.builder().bucket(bucketName).key(document.getFilePath()).build());
        documentRepository.deleteById(documentId);
    }

    public boolean isOwner(Integer documentId) {
        String userId = CurrentUserUtils.getUserId();
        return userId != null && documentRepository.existsByIdAndCreatedBy_Id(documentId, userId);
    }

    public boolean canView(Integer documentId) {
        String userId = CurrentUserUtils.getUserId();
        if (userId == null) return false;
        if (isAdmin()) return true;
        User user = userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        return documentRepository.existsByIdAndCreatedBy_Id(documentId, userId)
            || documentRepository.existsByIdAndDepartment_Id(documentId, user.getDepartmentId());
    }

    public boolean isAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    private String generateUniqueFileName(String originalFileName) {
        String timestamp = String.valueOf(System.currentTimeMillis());
        if (originalFileName == null) return timestamp + "_" + java.util.UUID.randomUUID();
        return timestamp + "_" + originalFileName.replaceAll("\\s+", "_");
    }
    
    private DocumentResponse convertToResponse(Document doc) {
        return DocumentResponse.builder()
                .id(doc.getId()).title(doc.getTitle()).type(doc.getType())
                .departmentId(doc.getDepartment().getId()).departmentName(doc.getDepartment().getName())
                .createdBy(doc.getCreatedBy() != null ? doc.getCreatedBy().getId() : null)
                .createdByName(doc.getCreatedBy() != null ? doc.getCreatedBy().getUsername() : "Anonymous")
                .fileName(doc.getFileName()).fileSize(doc.getFileSize()).fileType(doc.getFileType())
                .currentVersion(doc.getCurrentVersion()).createdAt(doc.getCreatedAt()).build();
    }

    @Transactional(readOnly = true)
    public List<DocumentResponse> searchDocuments(String title, String type, Integer departmentId, LocalDateTime startDate, LocalDateTime endDate) {
        List<Document> documents = documentRepository.searchDocuments(title, type, departmentId, startDate, endDate);
        if (isAdmin()) return documents.stream().map(this::convertToResponse).toList();
        String userId = CurrentUserUtils.getUserId();
        User user = userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        Integer deptId = user.getDepartmentId();
        return documents.stream().filter(d -> (d.getCreatedBy() != null && d.getCreatedBy().getId().equals(userId)) || d.getDepartment().getId().equals(deptId)).map(this::convertToResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<DocumentResponse> getMyDocuments(String userId) {
        return documentRepository.findByCreatedById(userId).stream().map(this::convertToResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<DocumentResponse> getAllDocuments() {
        if (!isAdmin()) throw new AppException(ErrorCode.FORBIDDEN);
        return documentRepository.findAll().stream().map(this::convertToResponse).toList();
    }
    
    @Transactional(readOnly = true)
    public List<DocumentResponse> getDocumentsByDepartment(Integer departmentId) {
        String userId = CurrentUserUtils.getUserId();
        User user = userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        if (!isAdmin() && !user.getDepartmentId().equals(departmentId)) throw new AppException(ErrorCode.FORBIDDEN);
        return documentRepository.findByDepartmentId(departmentId).stream().map(this::convertToResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<DocumentResponse> getDocumentsByType(String type) {
        List<Document> documents = documentRepository.findByType(type);
        if (isAdmin()) return documents.stream().map(this::convertToResponse).toList();
        String userId = CurrentUserUtils.getUserId();
        User user = userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        Integer deptId = user.getDepartmentId();
        return documents.stream().filter(d -> (d.getCreatedBy() != null && d.getCreatedBy().getId().equals(userId)) || d.getDepartment().getId().equals(deptId)).map(this::convertToResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<DocumentVersionResponse> getDocumentVersions(Integer documentId) {
        if (!canView(documentId)) throw new AppException(ErrorCode.FORBIDDEN);
        return versionRepository.findByDocumentIdOrderByVersionNumberDesc(documentId).stream()
                .map(v -> DocumentVersionResponse.builder()
                        .id(v.getId()).versionNumber(v.getVersionNumber()).fileName(v.getFileName())
                        .fileSize(v.getFileSize()).fileType(v.getFileType())
                        .uploadBy(v.getUploadBy() != null ? v.getUploadBy().getId() : null)
                        .uploadByName(v.getUploadBy() != null ? v.getUploadBy().getUsername() : "Anonymous")
                        .createdAt(v.getCreatedAt()).build()).toList();
    }
}
