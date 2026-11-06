package com.smartbuilding.audit.service;

import com.smartbuilding.audit.model.AuditLog;
import com.smartbuilding.audit.repository.AuditLogRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public void log(String userId, String action, String entityType, String entityId, String details) {
        AuditLog auditLog = new AuditLog(
                UUID.randomUUID().toString(),
                userId,
                action,
                entityType,
                entityId,
                details
        );
        auditLogRepository.save(auditLog);
    }
}

