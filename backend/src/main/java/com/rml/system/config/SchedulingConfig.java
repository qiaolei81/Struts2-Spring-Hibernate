package com.rml.system.config;

import com.rml.system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDateTime;

@Configuration
@EnableScheduling
@RequiredArgsConstructor
public class SchedulingConfig {

    private final UserRepository userRepository;

    /**
     * ADR-13: Users inactive for >30 minutes are treated as offline.
     * This scheduled task runs every 5 minutes to clear stale lastActivity values.
     */
    @Scheduled(fixedDelay = 300_000)
    public void clearInactiveUsers() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(30);
        userRepository.findAll().stream()
            .filter(u -> u.getLastActivity() != null && u.getLastActivity().isBefore(threshold))
            .forEach(u -> {
                u.setLastActivity(null);
                userRepository.save(u);
            });
    }
}
