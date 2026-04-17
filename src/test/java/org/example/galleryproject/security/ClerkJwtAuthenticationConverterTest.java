package org.example.galleryproject.security;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ClerkJwtAuthenticationConverterTest {

    @Test
    void addsGuestRoleForAnyToken() {
        ClerkJwtAuthenticationConverter converter = new ClerkJwtAuthenticationConverter("");

        Authentication authentication = converter.convert(jwtWithSubject("user_123"));

        assertTrue(authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_GUEST")));
    }

    @Test
    void addsAdminRoleWhenRoleClaimContainsAdmin() {
        ClerkJwtAuthenticationConverter converter = new ClerkJwtAuthenticationConverter("");

        Authentication authentication = converter.convert(
                Jwt.withTokenValue("token")
                        .header("alg", "none")
                        .subject("user_123")
                        .issuedAt(Instant.now())
                        .expiresAt(Instant.now().plusSeconds(3600))
                        .claim("role", "admin")
                        .build()
        );

        assertTrue(authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN")));
    }

    private Jwt jwtWithSubject(String subject) {
        return Jwt.withTokenValue("token")
                .header("alg", "none")
                .subject(subject)
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();
    }
}
