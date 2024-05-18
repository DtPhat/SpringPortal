package com.swd.uniportal.infrastructure.config.security.authentication;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import java.time.Instant;
import org.apache.commons.lang3.Validate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public final class JwtService {

    @Value("${jwt.token.lifetime}")
    private Long tokenLifetime;

    @Value("${jwt.token.secret}")
    private String tokenSecret;

    @Value("${jwt.token.issuer}")
    private String tokenIssuer;

    public String generateToken(UserDetails userDetails) {
        Validate.notNull(userDetails, "User details is null when generating JWT token.");
        Instant issuedAt = Instant.now();
        return JWT.create()
                .withIssuer(tokenIssuer)
                .withSubject(userDetails.getUsername())
                .withIssuedAt(issuedAt)
                .withExpiresAt(issuedAt.plusMillis(tokenLifetime))
                .sign(Algorithm.HMAC256(tokenSecret));
    }

    /**
     * Validates the JWT token in String format and returns the subject from it.
     *
     * @param token                         JWT token as String.
     * @return                              The subject of the token.
     */
    public String validateJwtToken(String token) {
        Validate.notBlank(token, "Token is null or blank when validating.");
        try {
            DecodedJWT decodedJWT = JWT.require(Algorithm.HMAC256(tokenSecret))
                    .withIssuer(tokenIssuer)
                    .build()
                    .verify(token);
            String subject = decodedJWT.getSubject();
            Validate.notBlank(subject);
            return subject;
        } catch (Exception e) {
            return null;
        }
    }
}
