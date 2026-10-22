package fr.ulco.hotelbooking.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import fr.ulco.hotelbooking.persistence.entity.Compte;

public interface CompteRepository extends JpaRepository<Compte, Long> {
}