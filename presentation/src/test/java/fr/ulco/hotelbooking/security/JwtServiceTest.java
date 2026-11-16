package fr.ulco.hotelbooking.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.test.context.ActiveProfiles;

import fr.ulco.hotelbooking.dto.TokenDto;

@SpringBootTest
@ActiveProfiles("test")
class JwtServiceTest {

    private static final String EMAIL = "admin-test@example.invalid";

    @Autowired
    private JwtService jwtService;

    @Autowired
    private JwtDecoder jwtDecoder;

    @Autowired
    private JwtEncoder jwtEncoder;

    @Value("${security.jwt.issuer}")
    private String issuer;

    @Value("${security.jwt.audience}")
    private String audience;

    @Test
    void testGenereUnJetonValideAvecLesRoles() {
        TokenDto resultat = jwtService.generer(administrateur());

        Jwt jwt = jwtDecoder.decode(resultat.accessToken());

        assertEquals("Bearer", resultat.tokenType());
        assertEquals(900L, resultat.expiresIn());
        assertEquals(EMAIL, jwt.getSubject());
        assertEquals(issuer, jwt.getClaimAsString("iss"));
        assertEquals(List.of(audience), jwt.getAudience());
        assertEquals(
                List.of("ROLE_ADMIN"),
                jwt.getClaimAsStringList("roles")
        );
    }

    @Test
    void testRefuseUnJetonDontLaSignatureEstModifiee() {
        String jeton = jwtService
                .generer(administrateur())
                .accessToken();

        String[] parties = jeton.split("\\.");
        String signature = parties[2];

        String signatureModifiee =
                (signature.charAt(0) == 'a' ? "b" : "a")
                + signature.substring(1);

        String jetonModifie = parties[0]
                + "." + parties[1]
                + "." + signatureModifiee;

        assertThrows(
                JwtException.class,
                () -> jwtDecoder.decode(jetonModifie)
        );
    }

    @Test
    void testRefuseUnJetonExpire() {
        Instant maintenant = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .subject(EMAIL)
                .audience(List.of(audience))
                .issuedAt(maintenant.minusSeconds(600))
                .notBefore(maintenant.minusSeconds(600))
                .expiresAt(maintenant.minusSeconds(120))
                .claim("roles", List.of("ROLE_ADMIN"))
                .build();

        String jeton = jwtEncoder.encode(
                JwtEncoderParameters.from(
                        JwsHeader.with(MacAlgorithm.HS256)
                                .type("JWT")
                                .build(),
                        claims
                )
        ).getTokenValue();

        assertThrows(
                JwtException.class,
                () -> jwtDecoder.decode(jeton)
        );
    }

    @Test
    void testRefuseDeGenererUnJetonSansAuthentification() {
        Authentication nonAuthentifie =
                UsernamePasswordAuthenticationToken.unauthenticated(
                        EMAIL,
                        "non-utilise"
                );

        Authentication anonyme = new AnonymousAuthenticationToken(
                "cle-de-test",
                "anonymousUser",
                List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> jwtService.generer(nonAuthentifie)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> jwtService.generer(anonyme)
        );
    }

    private Authentication administrateur() {
        return UsernamePasswordAuthenticationToken.authenticated(
                EMAIL,
                null,
                List.of(
                        new SimpleGrantedAuthority("ROLE_ADMIN"),
                        new SimpleGrantedAuthority("FACTOR_PASSWORD")
                )
        );
    }
}