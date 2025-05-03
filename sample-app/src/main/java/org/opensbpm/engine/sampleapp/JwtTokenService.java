package org.opensbpm.engine.sampleapp;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class JwtTokenService {

    private final JwtEncoder jwtEncoder;

    public JwtTokenService(JwtEncoder jwtEncoder) {
        this.jwtEncoder = jwtEncoder;
    }

    public String generateToken(Authentication authentication) {
        Instant now = Instant.now();

        List<String> roles = authentication.getAuthorities().stream()
                .map(grantedAuthority -> grantedAuthority.getAuthority().substring("ROLE_".length()))
                .toList();

        // Define claims
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(authentication.getName())
                .claim("preferred_username", authentication.getName())
                .claim("scope", roles)
                .issuedAt(now)
                .expiresAt(now.plusSeconds(3600)) // 1 hour expiration
                .build();

        // Encode the token
        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }
}
