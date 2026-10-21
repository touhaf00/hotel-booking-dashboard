package fr.ulco.hotelbooking.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import fr.ulco.hotelbooking.persistence.entity.Reservation;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
}