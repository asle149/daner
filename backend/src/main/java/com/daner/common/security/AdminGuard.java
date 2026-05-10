package com.daner.common.security;

import com.daner.common.exception.BusinessException;
import com.daner.common.exception.ErrorCode;
import com.daner.user.entity.User;
import com.daner.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 관리자 권한 검증 헬퍼.
 * - requireAdmin: 비관리자면 FORBIDDEN 던짐, 관리자면 User 반환
 * - findIfAdmin: 관리자면 User 반환, 아니면 빈 Optional (분기용)
 */
@Component
@RequiredArgsConstructor
public class AdminGuard {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public User requireAdmin(Long currentUserId) {
        if (currentUserId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        if (!user.isAdmin()) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        return user;
    }

    @Transactional(readOnly = true)
    public Optional<User> findIfAdmin(Long currentUserId) {
        if (currentUserId == null) return Optional.empty();
        return userRepository.findById(currentUserId).filter(User::isAdmin);
    }
}
