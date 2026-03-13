package com.rml.system.repository;

import com.rml.system.entity.Menu;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MenuRepository extends JpaRepository<Menu, String> {

    List<Menu> findByParentIsNullOrderBySequenceAsc();
}
