package com.rml.system.repository;

import com.rml.system.entity.Equipment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EquipmentRepository extends JpaRepository<Equipment, String> {

    @Query("SELECT e FROM Equipment e WHERE " +
           "(:q IS NULL OR LOWER(e.model) LIKE LOWER(CONCAT('%', :q, '%')) " +
           "OR LOWER(e.name) LIKE LOWER(CONCAT('%', :q, '%')) " +
           "OR LOWER(e.producer) LIKE LOWER(CONCAT('%', :q, '%')))")
    Page<Equipment> search(@Param("q") String q, Pageable pageable);
}
