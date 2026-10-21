package fr.ulco.hotelbooking.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import fr.ulco.hotelbooking.persistence.entity.Ressource;

public interface RessourceRepository extends JpaRepository<Ressource, Long> {
}