package com.cnpmnc.document_management.repository;

import com.cnpmnc.document_management.entity.DocumentVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentVersionRepository extends JpaRepository<DocumentVersion, Integer> {
    List<DocumentVersion> findByDocumentIdOrderByVersionNumberDesc(Integer documentId);
}
