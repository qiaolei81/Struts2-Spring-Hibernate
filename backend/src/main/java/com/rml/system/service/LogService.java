package com.rml.system.service;

import com.rml.system.dto.response.LogDto;
import com.rml.system.entity.AccessLog;
import com.rml.system.repository.AccessLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class LogService {

    private final AccessLogRepository accessLogRepository;

    public Page<LogDto> listLogs(String q, Pageable pageable) {
        Page<AccessLog> page = (q != null && !q.isBlank())
            ? accessLogRepository.findByUsernameContainingIgnoreCase(q, pageable)
            : accessLogRepository.findAll(pageable);
        return page.map(this::toDto);
    }

    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    public void recordLog(String username, String ip, String message) {
        AccessLog log = new AccessLog();
        log.setUsername(username != null ? username : "anonymous");
        log.setIp(ip);
        log.setMessage(message);
        accessLogRepository.save(log);
    }

    private LogDto toDto(AccessLog log) {
        LogDto dto = new LogDto();
        dto.setId(log.getId());
        dto.setUsername(log.getUsername());
        dto.setIp(log.getIp());
        dto.setAccessedAt(log.getAccessedAt());
        dto.setMessage(log.getMessage());
        dto.setCreatedAt(log.getCreatedAt());
        return dto;
    }
}
