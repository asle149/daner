package com.daner.auth.service;

import com.daner.auth.dto.AccessTokenResponse;
import com.daner.auth.dto.AuthTokensResponse;
import com.daner.auth.dto.NicknameCheckResponse;
import com.daner.auth.dto.SignupRequest;
import com.daner.auth.dto.SignupTokenPayload;
import com.daner.common.exception.BusinessException;
import com.daner.common.exception.ErrorCode;
import com.daner.user.entity.User;
import com.daner.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final Pattern NICKNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9가-힣]+$");
    private static final int NICKNAME_MIN = 2;
    private static final int NICKNAME_MAX = 12;

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;

    @Transactional
    public AuthTokensResponse signup(SignupRequest request) {
        SignupTokenPayload payload = jwtTokenProvider.parseSignupToken(request.signupToken());
        validateNickname(request.nickname());
        if (userRepository.existsByNickname(request.nickname())) {
            throw new BusinessException(ErrorCode.DUPLICATE_NICKNAME);
        }
        User user = userRepository.save(User.builder()
                .oauthProvider(payload.oauthProvider())
                .oauthId(payload.oauthId())
                .nickname(request.nickname())
                .profileImageUrl(payload.profileImageUrl())
                .build());
        return issueTokens(user);
    }

    @Transactional
    public AuthTokensResponse issueTokens(User user) {
        String accessToken = jwtTokenProvider.createAccessToken(user.getId());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId());
        refreshTokenService.store(user, refreshToken);
        return AuthTokensResponse.of(user, accessToken, refreshToken);
    }

    @Transactional
    public AccessTokenResponse refresh(String refreshToken) {
        Long userId = jwtTokenProvider.parseRefreshToken(refreshToken);
        if (!refreshTokenService.isStored(refreshToken)) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }
        userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        return new AccessTokenResponse(jwtTokenProvider.createAccessToken(userId));
    }

    @Transactional
    public void logout(Long userId) {
        refreshTokenService.revokeAllForUser(userId);
    }

    @Transactional(readOnly = true)
    public NicknameCheckResponse checkNickname(String nickname) {
        if (nickname == null || nickname.isBlank()) {
            return NicknameCheckResponse.rejected("닉네임을 입력해주세요.");
        }
        if (nickname.length() < NICKNAME_MIN || nickname.length() > NICKNAME_MAX) {
            return NicknameCheckResponse.rejected("닉네임은 2~12자여야 합니다.");
        }
        if (!NICKNAME_PATTERN.matcher(nickname).matches()) {
            return NicknameCheckResponse.rejected("닉네임은 한글, 영문, 숫자만 가능합니다.");
        }
        if (userRepository.existsByNickname(nickname)) {
            return NicknameCheckResponse.rejected("이미 사용 중인 닉네임입니다.");
        }
        return NicknameCheckResponse.ok();
    }

    private void validateNickname(String nickname) {
        if (nickname == null || nickname.length() < NICKNAME_MIN || nickname.length() > NICKNAME_MAX
                || !NICKNAME_PATTERN.matcher(nickname).matches()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "닉네임 형식이 올바르지 않습니다.");
        }
    }
}
