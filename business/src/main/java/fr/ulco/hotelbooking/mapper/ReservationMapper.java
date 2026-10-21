package fr.ulco.hotelbooking.mapper;

import org.springframework.stereotype.Component;

import fr.ulco.hotelbooking.dto.ReservationDto;
import fr.ulco.hotelbooking.persistence.entity.Reservation;

@Component
public class ReservationMapper {

    public ReservationDto toDto(Reservation reservation) {
        return new ReservationDto(
                reservation.getId(),
                reservation.getDateDebut(),
                reservation.getDateFin(),
                reservation.getStatut(),
                reservation.getPrixSejour(),
                reservation.getClient().getId(),
                reservation.getCompte().getId(),
                reservation.getRessource().getId()
        );
    }
}