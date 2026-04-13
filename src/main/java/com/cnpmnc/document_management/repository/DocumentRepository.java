package com.cnpmnc.document_management.repository;

import com.cnpmnc.document_management.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Integer> {
    List<Document> findByDepartmentId(Integer departmentId);
    List<Document> findByType(String type);
    List<Document> findByCreatedById(UUID userId);


@Query(value = "SELECT d FROM Document d WHERE " +
           "(:title IS NULL OR LOWER(d.title) LIKE LOWER(CONCAT('%', :title, '%'))) AND " +
           "(:type IS NULL OR d.type = :type) AND " +
           "(:departmentId IS NULL OR d.department.id = :departmentId) AND " +
           "(CAST(:startDate AS timestamp) IS NULL OR d.createdAt >= :startDate) AND " +
           "(CAST(:endDate AS timestamp) IS NULL OR d.createdAt <= :endDate) " +
           "ORDER BY d.createdAt DESC")
    List<Document> searchDocuments(@Param("title") String title,
                                   @Param("type") String type,
                                   @Param("departmentId") Integer departmentId,
                                   @Param("startDate") LocalDateTime startDate,
                                   @Param("endDate") LocalDateTime endDate);
}

