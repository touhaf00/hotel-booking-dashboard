package fr.ulco.hotelbooking.controller.api;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fr.ulco.hotelbooking.dto.DashboardDto;
import fr.ulco.hotelbooking.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/api/dashboard")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Dashboard", description = "Statistiques générales de l'hôtel")
public class DashboardRestController {

    private final DashboardService dashboardService;

    public DashboardRestController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Consulter les compteurs du dashboard")
    @ApiResponse(
            responseCode = "200",
            description = "Statistiques récupérées"
    )
    public ResponseEntity<DashboardDto> consulterStatistiques() {
        return ResponseEntity.ok(
                dashboardService.obtenirStatistiques()
        );
    }
}