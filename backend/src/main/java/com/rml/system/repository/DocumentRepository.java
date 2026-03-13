package com.rml.system.repository;

import com.rml.system.entity.Document;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DocumentRepository extends JpaRepository<Document, String> {

    @Query("SELECT d FROM Document d WHERE " +
           "(:q IS NULL OR LOWER(d.model) LIKE LOWER(CONCAT('%', :q, '%')) " +
           "OR LOWER(d.name) LIKE LOWER(CONCAT('%', :q, '%')))")
    Page<Document> search(@Param("q") String q, Pageable pageable);
}
