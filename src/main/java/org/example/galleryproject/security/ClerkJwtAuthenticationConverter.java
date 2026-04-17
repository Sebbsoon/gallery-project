package org.example.galleryproject.security;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
public class ClerkJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private static final String ROLE_ADMIN = "ROLE_ADMIN";
    private static final String ROLE_GUEST = "ROLE_GUEST";

    private final Set<String> adminUserIds;

    public ClerkJwtAuthenticationConverter(@Value("${security.admin-user-ids:}") String adminUserIdsCsv) {
        this.adminUserIds = Arrays.stream(adminUserIdsCsv.split(","))
                .map(String::trim)
                .filter(id -> !id.isBlank())
                .collect(LinkedHashSet::new, Set::add, Set::addAll);
    }

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(ROLE_GUEST));

        if (isAdmin(jwt)) {
            authorities.add(new SimpleGrantedAuthority(ROLE_ADMIN));
        }

        return new JwtAuthenticationToken(jwt, authorities, jwt.getSubject());
    }

    private boolean isAdmin(Jwt jwt) {
        String subject = jwt.getSubject();
        if (subject != null && adminUserIds.contains(subject)) {
            return true;
        }
        return claimContainsAdmin(jwt.getClaim("role"))
                || claimContainsAdmin(jwt.getClaim("roles"))
                || claimContainsAdmin(jwt.getClaim("org_role"))
                || claimContainsAdmin(jwt.getClaim("org_roles"));
    }

    private boolean claimContainsAdmin(Object claimValue) {
        if (claimValue == null) {
            return false;
        }

        if (claimValue instanceof String value) {
            return value.toLowerCase().contains("admin");
        }

        if (claimValue instanceof Collection<?> values) {
            return values.stream()
                    .filter(item -> item != null)
                    .map(Object::toString)
                    .map(String::toLowerCase)
                    .anyMatch(item -> item.contains("admin"));
        }

        if (claimValue instanceof Map<?, ?> map) {
            return Stream.concat(map.keySet().stream(), map.values().stream())
                    .filter(item -> item != null)
                    .map(Object::toString)
                    .map(String::toLowerCase)
                    .anyMatch(item -> item.contains("admin"));
        }

        return false;
    }
}
