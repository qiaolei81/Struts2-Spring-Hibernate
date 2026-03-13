package com.rml.system.controller;

import com.rml.system.dto.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Liveness check endpoint — not protected by JWT.
 * Accessible at GET /api/health (context-path included automatically).
 *
 * NOTE: Spring Boot Actuator /actuator/health is also available.
 *       This endpoint exists as a simple smoke-test for the scaffold.
 */
@RestController
@RequestMapping("/health")
public class HealthController {

    @GetMapping
    public ApiResponse<Map<String, String>> health() {
        return ApiResponse.ok(Map.of(
                "status", "UP",
                "timestamp", LocalDateTime.now().toString()
        ));
    }
}
