package fr.ulco.hotelbooking.security;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import fr.ulco.hotelbooking.dto.TokenDto;

@Service
public class JwtService {

    private final JwtEncoder jwtEncoder;
    private final String issuer;
    private final String audience;
    private final long expirationSeconds;

    public JwtService(
            JwtEncoder jwtEncoder,
            @Value("${security.jwt.issuer}") String issuer,
            @Value("${security.jwt.audience}") String audience,
            @Value("${security.jwt.expiration-seconds}")
            long expirationSeconds) {

        if (expirationSeconds <= 0) {
            throw new IllegalArgumentException(
                    "La duree de validite du JWT doit etre positive."
            );
        }

        this.jwtEncoder = jwtEncoder;
        this.issuer = issuer;
        this.audience = audience;
        this.expirationSeconds = expirationSeconds;
    }

    public TokenDto generer(Authentication authentication) {
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {

            throw new IllegalArgumentException(
                    "Une authentification valide est necessaire."
            );
        }

        Instant maintenant = Instant.now();

        List<String> roles = authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .filter(autorite -> autorite.startsWith("ROLE_"))
                .toList();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .id(UUID.randomUUID().toString())
                .issuer(issuer)
                .subject(authentication.getName())
                .audience(List.of(audience))
                .issuedAt(maintenant)
                .notBefore(maintenant)
                .expiresAt(maintenant.plusSeconds(expirationSeconds))
                .claim("roles", roles)
                .build();

        JwsHeader entete = JwsHeader
                .with(MacAlgorithm.HS256)
                .type("JWT")
                .build();

        Jwt jwt = jwtEncoder.encode(
                JwtEncoderParameters.from(entete, claims)
        );

        return new TokenDto(
                jwt.getTokenValue(),
                "Bearer",
                expirationSeconds
        );
    }
}