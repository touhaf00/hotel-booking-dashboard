package fr.ulco.hotelbooking.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ReservationDto(
        Long id,
        LocalDate dateDebut,
        LocalDate dateFin,
        String statut,
        BigDecimal prixSejour,
        Long clientId,
        Long compteId,
        Long ressourceId
) {
}