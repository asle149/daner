package com.daner.common.config;

import com.daner.user.entity.User;
import com.daner.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 부팅 시 ADMIN_EMAILS 에 해당하는 사용자를 ADMIN 으로 승격.
 * 기존 가입자가 한 번도 재로그인 안 해서 email 이 NULL 이면 매칭 안 됨 — 다음 로그인 후 다음 부팅에서 잡힘.
 */
@Configuration
@RequiredArgsConstructor
public class AdminInitializer {

    private final AdminPromoter promoter;

    @Bean
    ApplicationRunner promoteAdminsRunner() {
        return args -> promoter.promote();
    }

    /** @Transactional 이 self-invocation 에서 무시되지 않도록 별도 컴포넌트로 분리 */
    @Slf4j
    @Component
    @RequiredArgsConstructor
    static class AdminPromoter {

        private final AdminProperties adminProperties;
        private final UserRepository userRepository;

        @Transactional
        public void promote() {
            List<String> targets = adminProperties.emailList();
            if (targets.isEmpty()) {
                log.info("ADMIN_EMAILS 가 비어 있음 — 관리자 승격 건너뜀");
                return;
            }

            List<User> matched = userRepository.findByEmailInIgnoreCase(targets);
            int promoted = 0;
            for (User user : matched) {
                if (!user.isAdmin()) {
                    user.promoteToAdmin();
                    promoted++;
                    log.info("관리자 승격: userId={} email={}", user.getId(), user.getEmail());
                }
            }
            log.info("ADMIN 승격 완료: 대상={}개, 매칭={}명, 새로 승격={}명",
                    targets.size(), matched.size(), promoted);
        }
    }
}
