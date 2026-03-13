package com.rml.system.repository;

import com.rml.system.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    Page<User> findByUsernameContainingIgnoreCase(String q, Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.lastActivity >= :since")
    List<User> findActiveUsers(@Param("since") LocalDateTime since);

    /**
     * Single aggregate query replacing the N+1 getRoleStats() loop.
     * Returns [roleName, userCount] pairs — one row per role.
     */
    @Query("SELECT r.name, COUNT(u.id) FROM User u JOIN u.roles r GROUP BY r.name")
    List<Object[]> countUsersByRole();

    /**
     * Bulk UPDATE to clear stale lastActivity values in a single SQL statement.
     * Replaces the O(N) findAll + per-entity save loop in SchedulingConfig.
     */
    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.lastActivity = null WHERE u.lastActivity IS NOT NULL AND u.lastActivity < :threshold")
    int clearActivityBefore(@Param("threshold") LocalDateTime threshold);
}
