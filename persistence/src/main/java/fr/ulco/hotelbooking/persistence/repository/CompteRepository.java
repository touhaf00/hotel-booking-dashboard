package fr.ulco.hotelbooking.persistence.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import fr.ulco.hotelbooking.persistence.entity.Compte;

public interface CompteRepository extends JpaRepository<Compte, Long> {

    @EntityGraph(attributePaths = "role")
    Optional<Compte> findByEmail(String email);
}