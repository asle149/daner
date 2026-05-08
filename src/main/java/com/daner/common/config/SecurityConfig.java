package com.daner.common.config;

import com.daner.auth.service.CustomOAuth2UserService;
import com.daner.auth.service.JwtAuthenticationFilter;
import com.daner.auth.service.JwtTokenProvider;
import com.daner.auth.service.OAuth2SuccessHandler;
import com.daner.common.exception.ErrorCode;
import com.daner.common.response.ApiResponse;
import com.daner.common.response.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final ObjectMapper objectMapper;

    @Value("${app.cors.allowed-origins}")
    private String allowedOrigins;

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/auth/google", "/auth/google/**",
                                "/auth/signup", "/auth/check-nickname", "/auth/refresh",
                                "/oauth2/**",
                                "/actuator/health"
                        ).permitAll()
                        .requestMatchers(HttpMethod.GET,
                                "/home", "/words/*", "/words/*/comments",
                                "/comments/*/replies", "/users/*"
                        ).permitAll()
                        .requestMatchers(HttpMethod.POST,
                                "/words/*/comments", "/comments/*/replies"
                        ).permitAll()
                        .anyRequest().authenticated())
                .oauth2Login(oauth -> oauth
                        .redirectionEndpoint(endpoint -> endpoint.baseUri("/auth/{registrationId}/callback"))
                        .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                        .successHandler(oAuth2SuccessHandler))
                .exceptionHandling(handlers -> handlers
                        .authenticationEntryPoint((request, response, ex) -> writeError(response, ErrorCode.UNAUTHORIZED))
                        .accessDeniedHandler((request, response, ex) -> writeError(response, ErrorCode.FORBIDDEN)))
                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider),
                        UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cors = new CorsConfiguration();
        cors.setAllowedOrigins(Arrays.stream(allowedOrigins.split(",")).map(String::trim).toList());
        cors.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        cors.setAllowedHeaders(List.of("*"));
        cors.setExposedHeaders(List.of("Authorization"));
        cors.setAllowCredentials(true);
        cors.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cors);
        return source;
    }

    private void writeError(jakarta.servlet.http.HttpServletResponse response, ErrorCode code) throws java.io.IOException {
        response.setStatus(code.getHttpStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        ApiResponse<Void> body = ApiResponse.fail(new ErrorResponse(code.name(), code.getDefaultMessage()));
        objectMapper.writeValue(response.getWriter(), body);
    }
}
