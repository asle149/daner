package com.daner.admin.service;

import com.daner.admin.entity.AdminAuditLog;
import com.daner.admin.repository.AdminAuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminAuditService {

    private final AdminAuditLogRepository repository;

    /** 관리자 작업 기록 — REQUIRES_NEW 로 호출자 트랜잭션과 분리해 작업 실패해도 로그는 남김 */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(Long adminId, String action, String targetType, String targetId, String detail) {
        repository.save(AdminAuditLog.builder()
                .adminId(adminId)
                .action(action)
                .targetType(targetType)
                .targetId(targetId)
                .detail(truncate(detail))
                .build());
    }

    private String truncate(String s) {
        if (s == null) return null;
        return s.length() > 500 ? s.substring(0, 500) : s;
    }
}
