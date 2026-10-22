package fr.ulco.hotelbooking.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import fr.ulco.hotelbooking.persistence.entity.Client;

public interface ClientRepository extends JpaRepository<Client, Long> {
}