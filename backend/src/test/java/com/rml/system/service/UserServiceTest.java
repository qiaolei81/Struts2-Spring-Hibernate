package com.rml.system.service;

import com.rml.system.dto.response.RoleStatDto;
import com.rml.system.repository.RoleRepository;
import com.rml.system.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for UserService — no Spring context, mocked dependencies.
 * JwtTokenProvider and PasswordEncoder are passed as null because
 * the methods under test (getRoleStats) do not invoke them.
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;

    private UserService userService;

    @BeforeEach
    void setUp() {
        // JwtTokenProvider and PasswordEncoder not used by getRoleStats; pass null to
        // avoid Mockito inline-mock issues with concrete classes on Java 25.
        userService = new UserService(userRepository, roleRepository, null, null);
    }

    @Test
    @DisplayName("getRoleStats returns aggregate counts from single DB query")
    void getRoleStats_returnsAggregatedCounts() {
        when(userRepository.countUsersByRole()).thenReturn(List.of(
                new Object[]{"ADMIN", 2L},
                new Object[]{"USER", 5L}
        ));

        List<RoleStatDto> stats = userService.getRoleStats();

        assertThat(stats).hasSize(2);
        assertThat(stats).anySatisfy(s -> {
            assertThat(s.getName()).isEqualTo("ADMIN");
            assertThat(s.getCount()).isEqualTo(2L);
        });
        assertThat(stats).anySatisfy(s -> {
            assertThat(s.getName()).isEqualTo("USER");
            assertThat(s.getCount()).isEqualTo(5L);
        });
        // Only one DB round-trip — no findAll() N+1
        verify(userRepository).countUsersByRole();
    }

    @Test
    @DisplayName("getRoleStats returns 'No Role' sentinel when no role assignments exist")
    void getRoleStats_returnsNoRoleSentinelWhenEmpty() {
        when(userRepository.countUsersByRole()).thenReturn(List.of());

        List<RoleStatDto> stats = userService.getRoleStats();

        assertThat(stats).hasSize(1);
        assertThat(stats.get(0).getName()).isEqualTo("No Role");
        assertThat(stats.get(0).getCount()).isEqualTo(0L);
    }
}
