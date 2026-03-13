package com.rml.system.repository;

import com.rml.system.entity.AccessLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccessLogRepository extends JpaRepository<AccessLog, String> {

    Page<AccessLog> findByUsernameContainingIgnoreCase(String username, Pageable pageable);
}
