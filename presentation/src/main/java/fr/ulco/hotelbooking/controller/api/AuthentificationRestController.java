package fr.ulco.hotelbooking.controller.api;

import java.nio.charset.StandardCharsets;

import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fr.ulco.hotelbooking.dto.ConnexionDto;
import fr.ulco.hotelbooking.dto.TokenDto;
import fr.ulco.hotelbooking.security.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentification")
public class AuthentificationRestController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthentificationRestController(
            AuthenticationManager authenticationManager,
            JwtService jwtService) {

        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @PostMapping(
            value = "/login",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(summary = "Se connecter et obtenir un jeton JWT")
    @ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "Connexion réussie"
        ),
        @ApiResponse(
                responseCode = "400",
                description = "Données de connexion invalides",
                content = @Content
        ),
        @ApiResponse(
                responseCode = "401",
                description = "Identifiants incorrects",
                content = @Content
        )
    })
    public ResponseEntity<TokenDto> connecter(
            @Valid @RequestBody ConnexionDto connexion) {

        // Limite de longueur du mot de passe pour BCrypt.
        if (connexion.motDePasse()
                .getBytes(StandardCharsets.UTF_8).length > 72) {

            return ResponseEntity.badRequest()
                    .cacheControl(CacheControl.noStore())
                    .build();
        }

        try {
            Authentication authentication =
                    authenticationManager.authenticate(
                            UsernamePasswordAuthenticationToken
                                    .unauthenticated(
                                            connexion.email(),
                                            connexion.motDePasse()
                                    )
                    );

            return ResponseEntity.ok()
                    .cacheControl(CacheControl.noStore())
                    .body(jwtService.generer(authentication));

        } catch (AuthenticationException exception) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .cacheControl(CacheControl.noStore())
                    .build();
        }
    }
}