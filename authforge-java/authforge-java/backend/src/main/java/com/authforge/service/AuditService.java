package com.authforge.service;

import com.authforge.entity.AuditLog;
import com.authforge.repository.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    @Async
    public void log(Long userId, String action, HttpServletRequest request, boolean success, String details) {
        AuditLog log = AuditLog.builder()
                .userId(userId)
                .action(action)
                .ipAddress(getClientIp(request))
                .userAgent(truncate(request.getHeader("User-Agent"), 512))
                .success(success)
                .details(details)
                .build();
        auditLogRepository.save(log);
    }

    private String getClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        return (xff != null && !xff.isBlank()) ? xff.split(",")[0].trim() : request.getRemoteAddr();
    }

    private String truncate(String s, int max) {
        return s == null ? null : s.length() <= max ? s : s.substring(0, max);
    }
}
