package com.commercex.security;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
@EnableConfigurationProperties(JwtProperties.class)
public class SecurityConfig {

        private final CustomUserDetailsService userDetailsService;
        private final CustomAuthenticationEntryPoint authenticationEntryPoint;
        private final CustomAccessDeniedHandler accessDeniedHandler;
        private final JwtAuthenticationFilter jwtAuthenticationFilter;
        private final RateLimitingFilter rateLimitingFilter;

        @Value("${app.cors.allowed-origins}")
        private String allowedOrigins;

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                                .csrf(AbstractHttpConfigurer::disable)
                                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                                // HTTP Security Headers
                                .headers(headers -> headers
                                                .frameOptions(HeadersConfigurer.FrameOptionsConfig::deny)
                                                .contentTypeOptions(contentTypeOptions -> {
                                                })
                                                .httpStrictTransportSecurity(hsts -> hsts
                                                                .includeSubDomains(true)
                                                                .maxAgeInSeconds(31536000))
                                                .referrerPolicy(referrer -> referrer.policy(
                                                                ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
                                                .permissionsPolicy(permissions -> permissions
                                                                .policy("camera=(), microphone=(), geolocation=()")))
                                .exceptionHandling(exception -> exception
                                                .authenticationEntryPoint(authenticationEntryPoint)
                                                .accessDeniedHandler(accessDeniedHandler))
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers(SecurityConstants.PUBLIC_URLS).permitAll()
                                                .requestMatchers(HttpMethod.GET, SecurityConstants.PUBLIC_GET_URLS)
                                                .permitAll()
                                                // Actuator: health/info public, others require ADMIN
                                                .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                                                .requestMatchers("/actuator/**").hasAuthority("ROLE_ADMIN")
                                                .anyRequest().authenticated())
                                .authenticationProvider(authenticationProvider())
                                // Rate limiting before JWT authentication
                                .addFilterBefore(rateLimitingFilter, UsernamePasswordAuthenticationFilter.class)
                                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }

        @Bean
        public AuthenticationProvider authenticationProvider() {
                DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
                authProvider.setUserDetailsService(userDetailsService);
                authProvider.setPasswordEncoder(passwordEncoder());
                return authProvider;
        }

        @Bean
        public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
                return config.getAuthenticationManager();
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder(12);
        }

        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
                CorsConfiguration configuration = new CorsConfiguration();
                List<String> origins = Arrays.asList(allowedOrigins.split(","));
                configuration.setAllowedOrigins(origins);
                configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
                configuration.setAllowedHeaders(
                                List.of("Authorization", "Content-Type", "X-Correlation-ID", "X-Request-ID"));
                configuration.setExposedHeaders(List.of("X-Correlation-ID"));
                configuration.setAllowCredentials(true);
                configuration.setMaxAge(3600L);

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", configuration);
                return source;
        }
}
