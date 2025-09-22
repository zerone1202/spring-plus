package org.example.expert.config;

import org.example.expert.domain.common.dto.AuthUser;
import org.springframework.security.authentication.AbstractAuthenticationToken;

public class JwtAuthenticationToken extends AbstractAuthenticationToken {

    private final AuthUser authUser;

    // 인증된 JWT 토큰을 생성하는 생성자.
    public JwtAuthenticationToken(AuthUser authUser) {
        super(authUser.getAuthorities());
        this.authUser = authUser;
        setAuthenticated(true); // Spring Security에 사용자가 이미 인증되었음을 알려줌
    }

    // JWT 인증에서는 토큰 검증 후 자격 증명이 필요하지 않으므로 null을 반환합니다.
    @Override
    public Object getCredentials() {
        return null;
    }

    // Principal(인증된 사용자)을 반환합니다. (애플리케이션 전체에서 현재 사용자의 정보에 접근하는 데 사용)
    @Override
    public Object getPrincipal() {
        return authUser;
    }
}
