package com.commercex.security;

import com.commercex.entity.Role;
import com.commercex.entity.User;
import com.commercex.entity.enums.RoleName;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;
    private JwtProperties jwtProperties;

    @BeforeEach
    void setUp() {
        jwtProperties = new JwtProperties();
        jwtProperties.setSecret("404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970");
        jwtProperties.setExpirationMs(3600000); // 1 hour
        jwtProperties.setIssuer("test-issuer");
        jwtProperties.setAudience("test-audience");

        jwtTokenProvider = new JwtTokenProvider(jwtProperties);
    }

    private Authentication createMockAuthentication() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("test@example.com");
        user.setPassword("password");
        user.setEnabled(true);
        
        Role role = new Role();
        role.setName(RoleName.ROLE_CUSTOMER);
        user.setRoles(Set.of(role));

        CustomUserDetails userDetails = new CustomUserDetails(user);
        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }

    @Test
    void shouldGenerateValidToken() {
        Authentication auth = createMockAuthentication();
        String token = jwtTokenProvider.generateToken(auth);

        assertNotNull(token);
        assertTrue(jwtTokenProvider.validateToken(token));
        assertEquals("test@example.com", jwtTokenProvider.getUsernameFromToken(token));
    }

    @Test
    void shouldFailValidationOnInvalidToken() {
        String invalidToken = "ey.invalid.token";
        assertThrows(MalformedJwtException.class, () -> jwtTokenProvider.validateToken(invalidToken));
    }

    @Test
    void shouldFailValidationOnExpiredToken() throws InterruptedException {
        jwtProperties.setExpirationMs(1); // 1 millisecond
        jwtTokenProvider = new JwtTokenProvider(jwtProperties);
        
        Authentication auth = createMockAuthentication();
        String token = jwtTokenProvider.generateToken(auth);
        
        Thread.sleep(10); // wait to expire
        
        assertThrows(ExpiredJwtException.class, () -> jwtTokenProvider.validateToken(token));
    }
}
