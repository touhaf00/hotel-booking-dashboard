package fr.ulco.hotelbooking.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import fr.ulco.hotelbooking.persistence.entity.Chambre;

public interface ChambreRepository extends JpaRepository<Chambre, Long> {
}