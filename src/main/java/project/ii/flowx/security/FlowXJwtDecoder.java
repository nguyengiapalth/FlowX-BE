package project.ii.flowx.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;
import project.ii.flowx.model.repository.InvalidTokenRepository;

import javax.crypto.spec.SecretKeySpec;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;

@Component
public class FlowXJwtDecoder implements JwtDecoder {
    @Value("${spring.jwt.secret}")
    private String jwtSecret;

    @Autowired
    private InvalidTokenRepository invalidTokenRepository;

    private NimbusJwtDecoder nimbusJwtDecoder;

    @PostConstruct
    public void init() {
        SecretKeySpec secretKey = new SecretKeySpec(jwtSecret.getBytes(), "HmacSHA256");
        this.nimbusJwtDecoder = NimbusJwtDecoder.withSecretKey(secretKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
    }

    @Override
    public Jwt decode(String token) throws JwtException {
        if (invalidTokenRepository.existsByToken(token)) throw new JwtException("Token has been invalidated");

        try {
            // First verify with Auth0 JWT to ensure compatibility with existing code
            DecodedJWT decodedJWT = JWT.require(Algorithm.HMAC256(jwtSecret))
                    .build()
                    .verify(token);

            // Convert Auth0 JWT to Spring Security JWT
            Map<String, Object> headers = new HashMap<>();
            headers.put("alg", "HS256");
            headers.put("typ", "JWT");

            Map<String, Object> claims = new HashMap<>();
            claims.put("sub", decodedJWT.getSubject());

            // Add all claims from the decoded JWT
            decodedJWT.getClaims().forEach((key, value) -> {
                if (value.asString() != null) {
                    claims.put(key, value.asString());
                } else if (value.asLong() != null) {
                    claims.put(key, value.asLong());
                } else if (value.asBoolean() != null) {
                    claims.put(key, value.asBoolean());
                } else if (value.asDate() != null) {
                    claims.put(key, LocalDateTime.ofInstant(value.asDate().toInstant(), ZoneOffset.UTC));
                }
            });

            // Create a Spring Security JWT
            return Jwt.withTokenValue(token)
                    .headers(h -> h.putAll(headers))
                    .claims(c -> c.putAll(claims))
                    .issuedAt(decodedJWT.getIssuedAt() != null ? decodedJWT.getIssuedAt().toInstant() : LocalDateTime.now().toInstant(ZoneOffset.UTC))
                    .expiresAt(decodedJWT.getExpiresAt() != null ? decodedJWT.getExpiresAt().toInstant() : null)
                    .build();
        } catch (JWTVerificationException e) {
            throw new JwtException("Invalid JWT token: " + e.getMessage());
        } catch (Exception e) {
            throw new JwtException("Error decoding JWT token: " + e.getMessage());
        }
    }
}
