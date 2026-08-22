package com.commercex.service.impl;

import com.commercex.dto.AuthResponse;
import com.commercex.dto.GoogleOAuthRequest;
import com.commercex.dto.UserResponse;
import com.commercex.entity.RefreshToken;
import com.commercex.entity.Role;
import com.commercex.entity.User;
import com.commercex.entity.enums.RoleName;
import com.commercex.event.UserRegisteredEvent;
import com.commercex.exception.ResourceNotFoundException;
import com.commercex.mapper.UserMapper;
import com.commercex.repository.RoleRepository;
import com.commercex.repository.UserRepository;
import com.commercex.security.CustomUserDetails;
import com.commercex.security.JwtTokenProvider;
import com.commercex.service.GoogleOAuthService;
import com.commercex.service.RefreshTokenService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleOAuthServiceImpl implements GoogleOAuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final UserMapper userMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Value("${app.oauth2.google.client-id:default-google-client-id}")
    private String googleClientId;

    @Override
    @Transactional
    public AuthResponse authenticateWithGoogle(GoogleOAuthRequest request, HttpServletRequest httpRequest) {
        log.info("Processing Google OAuth2 login for email: {}", request.getEmail());
        log.debug("Google Client ID configured: [PROTECTED]");
        log.trace("Received Google OAuth payload with firstName: {}, lastName: {}", request.getFirstName(), request.getLastName());

        Optional<User> existingUserOpt = userRepository.findByEmail(request.getEmail());
        User user;
        boolean isNewUser = false;

        if (existingUserOpt.isPresent()) {
            user = existingUserOpt.get();
            log.debug("Existing user found for Google email: {}", user.getEmail());
        } else {
            isNewUser = true;
            log.info("Creating new user account for Google OAuth user: {}", request.getEmail());

            String firstName = request.getFirstName() != null && !request.getFirstName().isBlank()
                    ? request.getFirstName() : "GoogleUser";
            String lastName = request.getLastName() != null && !request.getLastName().isBlank()
                    ? request.getLastName() : "Member";

            Role customerRole = roleRepository.findByName(RoleName.ROLE_CUSTOMER)
                    .orElseGet(() -> {
                        log.warn("ROLE_CUSTOMER not found in DB, creating on the fly");
                        Role newRole = new Role();
                        newRole.setName(RoleName.ROLE_CUSTOMER);
                        return roleRepository.save(newRole);
                    });

            Set<Role> roles = new HashSet<>();
            roles.add(customerRole);

            // Generate strong random password for OAuth user
            String randomSecret = UUID.randomUUID().toString();

            user = User.builder()
                    .email(request.getEmail().toLowerCase().trim())
                    .firstName(firstName)
                    .lastName(lastName)
                    .password(passwordEncoder.encode(randomSecret))
                    .enabled(true)
                    .roles(roles)
                    .build();

            user = userRepository.save(user);
            log.info("Successfully provisioned new Google OAuth user with ID: {}", user.getId());
        }

        if (isNewUser) {
            eventPublisher.publishEvent(new UserRegisteredEvent(user.getId(), user.getEmail(), user.getFirstName()));
        }

        // Generate JWT Authentication and Tokens
        CustomUserDetails userDetails = new CustomUserDetails(user);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities()
        );

        String accessToken = jwtTokenProvider.generateToken(authentication);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(
                user.getId(),
                "Google-OAuth-Session",
                getClientIp(httpRequest),
                getUserAgent(httpRequest)
        );

        UserResponse userResponse = userMapper.toDto(user);
        log.info("Google OAuth authentication successful for user: {}", user.getEmail());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .user(userResponse)
                .build();
    }

    private String getClientIp(HttpServletRequest request) {
        if (request == null) return "0.0.0.0";
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }

    private String getUserAgent(HttpServletRequest request) {
        if (request == null) return "Unknown";
        return request.getHeader("User-Agent");
    }
}
