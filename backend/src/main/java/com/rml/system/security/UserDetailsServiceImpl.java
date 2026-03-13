package com.rml.system.security;

import com.rml.system.entity.Authority;
import com.rml.system.entity.Role;
import com.rml.system.entity.User;
import com.rml.system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * JPA-backed UserDetailsService. Loads user + roles + authorities from DB.
 * Uses UserRepository directly (not UserService) to avoid circular dependency
 * with AuthenticationManager.
 */
@Service
@Primary
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        List<GrantedAuthority> authorities = new ArrayList<>();

        // Add role-based authorities (ROLE_*)
        for (Role role : user.getRoles()) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName().toUpperCase()));
            // Add permission codes from the role's authorities (PERM_*)
            for (Authority auth : role.getAuthorities()) {
                if (auth.getUrl() != null && !auth.getUrl().isBlank()) {
                    authorities.add(new SimpleGrantedAuthority(auth.getUrl()));
                }
            }
        }

        return org.springframework.security.core.userdetails.User
            .withUsername(user.getUsername())
            .password(user.getPassword())
            .authorities(authorities)
            .build();
    }
}
