package com.rml.system.repository;

import com.rml.system.entity.Authority;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuthorityRepository extends JpaRepository<Authority, String> {

    List<Authority> findByParentIsNullOrderBySequenceAsc();
}
