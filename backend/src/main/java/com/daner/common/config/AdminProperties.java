package com.daner.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Arrays;
import java.util.List;

/**
 * 관리자 설정. 콤마로 구분된 ADMIN_EMAILS 환경변수를 받아 List 로 보관.
 *
 * application.yml:
 *   app.admin.emails: ${ADMIN_EMAILS:}
 */
@ConfigurationProperties(prefix = "app.admin")
public record AdminProperties(String emails) {

    public List<String> emailList() {
        if (emails == null || emails.isBlank()) {
            return List.of();
        }
        return Arrays.stream(emails.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(String::toLowerCase)
                .toList();
    }

    public boolean isAdminEmail(String email) {
        if (email == null || email.isBlank()) return false;
        return emailList().contains(email.trim().toLowerCase());
    }
}
