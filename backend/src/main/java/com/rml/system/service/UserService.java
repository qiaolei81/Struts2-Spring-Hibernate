package com.rml.system.service;

import com.rml.system.dto.request.AssignRolesRequest;
import com.rml.system.dto.request.CreateUserRequest;
import com.rml.system.dto.request.UpdateUserRequest;
import com.rml.system.dto.response.LoginResponse;
import com.rml.system.dto.response.RoleDto;
import com.rml.system.dto.response.RoleStatDto;
import com.rml.system.dto.response.UserDto;
import com.rml.system.entity.Role;
import com.rml.system.entity.User;
import com.rml.system.exception.AppException;
import com.rml.system.repository.RoleRepository;
import com.rml.system.repository.UserRepository;
import com.rml.system.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public Page<UserDto> listUsers(String q, Pageable pageable) {
        Page<User> page = (q != null && !q.isBlank())
            ? userRepository.findByUsernameContainingIgnoreCase(q, pageable)
            : userRepository.findAll(pageable);
        return page.map(this::toDto);
    }

    @Transactional(readOnly = true)
    public List<UserDto> listAll() {
        return userRepository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional
    public UserDto createUser(CreateUserRequest req) {
        if (userRepository.existsByUsername(req.getUsername())) {
            throw AppException.conflict("Username already exists: " + req.getUsername());
        }
        User user = new User();
        user.setUsername(req.getUsername());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        if (req.getRoleIds() != null && !req.getRoleIds().isEmpty()) {
            user.setRoles(new java.util.HashSet<>(roleRepository.findAllById(req.getRoleIds())));
        }
        return toDto(userRepository.save(user));
    }

    @Transactional
    public UserDto updateUser(String id, UpdateUserRequest req) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> AppException.notFound("User not found: " + id));
        if (req.getUsername() != null && !req.getUsername().equals(user.getUsername())) {
            if (userRepository.existsByUsername(req.getUsername())) {
                throw AppException.conflict("Username already exists: " + req.getUsername());
            }
            user.setUsername(req.getUsername());
        }
        if (req.getPassword() != null && !req.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(req.getPassword()));
        }
        if (req.getRoleIds() != null) {
            user.setRoles(new java.util.HashSet<>(roleRepository.findAllById(req.getRoleIds())));
        }
        return toDto(userRepository.save(user));
    }

    @Transactional
    public void deleteUsers(List<String> ids) {
        if (ids == null || ids.isEmpty()) return;
        List<String> filtered = ids.stream().filter(id -> !"0".equals(id)).collect(Collectors.toList());
        userRepository.deleteAllById(filtered);
    }

    @Transactional
    public void assignRoles(AssignRolesRequest req) {
        if (req.getUserIds() == null || req.getUserIds().isEmpty()) return;
        List<Role> roles = req.getRoleIds() != null
            ? roleRepository.findAllById(req.getRoleIds())
            : Collections.emptyList();
        Set<Role> roleSet = new HashSet<>(roles);
        for (String userId : req.getUserIds()) {
            userRepository.findById(userId).ifPresent(user -> {
                user.setRoles(roleSet);
                userRepository.save(user);
            });
        }
    }

    @Transactional(readOnly = true)
    public List<RoleStatDto> getRoleStats() {
        List<User> users = userRepository.findAll();
        Map<String, Long> counts = new HashMap<>();
        for (User user : users) {
            for (Role role : user.getRoles()) {
                counts.merge(role.getName(), 1L, Long::sum);
            }
        }
        if (counts.isEmpty()) {
            counts.put("No Role", 0L);
        }
        return counts.entrySet().stream()
            .map(e -> new RoleStatDto(e.getKey(), e.getValue()))
            .collect(Collectors.toList());
    }

    @Transactional
    public LoginResponse authenticate(String username, String password) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> AppException.unauthorized("Invalid username or password"));
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw AppException.unauthorized("Invalid username or password");
        }
        user.setLastActivity(LocalDateTime.now());
        userRepository.save(user);
        String token = jwtTokenProvider.generateAccessToken(user.getUsername());
        return new LoginResponse(token, toDto(user));
    }

    @Transactional(readOnly = true)
    public List<UserDto> getActiveUsers() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(30);
        return userRepository.findActiveUsers(threshold).stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }

    public UserDto toDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        dto.setLastActivity(user.getLastActivity());
        if (user.getRoles() != null) {
            dto.setRoles(user.getRoles().stream().map(role -> {
                RoleDto roleDto = new RoleDto();
                roleDto.setId(role.getId());
                roleDto.setName(role.getName());
                roleDto.setDescription(role.getDescription());
                return roleDto;
            }).collect(Collectors.toList()));
        }
        return dto;
    }
}
