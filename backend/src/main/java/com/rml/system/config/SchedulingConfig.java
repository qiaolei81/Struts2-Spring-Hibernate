package com.rml.system.config;

import com.rml.system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Configuration
@EnableScheduling
@RequiredArgsConstructor
public class SchedulingConfig {

    private final UserRepository userRepository;

    /**
     * ADR-13: Users inactive for >30 minutes are treated as offline.
     * Replaced O(N) findAll + per-entity save with a single bulk UPDATE statement.
     */
    @Scheduled(fixedDelay = 300_000)
    @Transactional
    public void clearInactiveUsers() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(30);
        userRepository.clearActivityBefore(threshold);
    }
}
