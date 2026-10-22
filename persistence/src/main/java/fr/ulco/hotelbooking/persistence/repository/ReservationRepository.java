package fr.ulco.hotelbooking.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import fr.ulco.hotelbooking.persistence.entity.Reservation;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
	List<Reservation> findTop5ByOrderByIdDesc();
}