package project.ii.flowx.security;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwtIssuerValidator;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;
import project.ii.flowx.model.repository.InvalidTokenRepository;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Component
public class FlowXJwtDecoder implements JwtDecoder {
    @Value("${spring.jwt.secret}")
    private String jwtSecret;

    @Value("${spring.jwt.issuer:FlowX}")
    private String jwtIssuer;

    @Autowired
    private InvalidTokenRepository invalidTokenRepository;

    private NimbusJwtDecoder nimbusJwtDecoder;

    @PostConstruct
    public void init() {
        SecretKeySpec secretKey = new SecretKeySpec(jwtSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        this.nimbusJwtDecoder = NimbusJwtDecoder.withSecretKey(secretKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();

        // Enable JWT validation with clock skew tolerance using DelegatingOAuth2TokenValidator
        OAuth2TokenValidator<Jwt> timestampValidator = new JwtTimestampValidator(Duration.ofSeconds(30));
        OAuth2TokenValidator<Jwt> issuerValidator = new JwtIssuerValidator(jwtIssuer);
        OAuth2TokenValidator<Jwt> withClockSkew = new DelegatingOAuth2TokenValidator<>(timestampValidator, issuerValidator);
        
        this.nimbusJwtDecoder.setJwtValidator(withClockSkew);
    }

    @Override
    public Jwt decode(String token) throws JwtException {
        if (invalidTokenRepository.existsByToken(token)) {
            throw new JwtException("Token has been invalidated");
        }

        try {
            return nimbusJwtDecoder.decode(token);
        } catch (JwtException e) {
            throw new JwtException("Invalid JWT token: " + e.getMessage(), e);
        }
    }
}
