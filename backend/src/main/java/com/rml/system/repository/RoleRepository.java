package com.rml.system.repository;

import com.rml.system.entity.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, String> {

    Page<Role> findByNameContainingIgnoreCase(String q, Pageable pageable);

    boolean existsByName(String name);
}
