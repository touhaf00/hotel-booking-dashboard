package fr.ulco.hotelbooking.controller.api;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fr.ulco.hotelbooking.dto.ReservationDto;
import fr.ulco.hotelbooking.service.ReservationService;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/api/reservations")
@SecurityRequirement(name = "bearerAuth")
public class ReservationRestController {

    private final ReservationService reservationService;

    public ReservationRestController(
            ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping
    public ResponseEntity<List<ReservationDto>> lister() {
        return ResponseEntity.ok(reservationService.lister());
    }
    
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Réservation trouvée"
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Identifiant invalide",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Réservation introuvable",
            content = @Content
        )
    })

    @GetMapping("/{id}")
    public ResponseEntity<ReservationDto> consulter(
            @PathVariable("id") Long id) {

        if (id <= 0) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.of(
                reservationService.trouverParId(id)
        );
    }
}