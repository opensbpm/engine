package org.opensbpm.engine.sampleapp;

import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class JwtTokenService {

    private final JwtEncoder jwtEncoder;

    public JwtTokenService(JwtEncoder jwtEncoder) {
        this.jwtEncoder = jwtEncoder;
    }

    public String generateToken(String username) {
        Instant now = Instant.now();

        // Define claims
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(username)
                .issuedAt(now)
                .expiresAt(now.plusSeconds(3600)) // 1 hour expiration
                .build();

        // Encode the token
        return jwtEncoder.encode(
                JwtEncoderParameters.from(claims)
        ).getTokenValue();
    }
}
