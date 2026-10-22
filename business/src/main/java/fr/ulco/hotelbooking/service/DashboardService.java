package fr.ulco.hotelbooking.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fr.ulco.hotelbooking.dto.DashboardDto;
import fr.ulco.hotelbooking.persistence.repository.ChambreRepository;
import fr.ulco.hotelbooking.persistence.repository.ClientRepository;
import fr.ulco.hotelbooking.persistence.repository.ReservationRepository;
import fr.ulco.hotelbooking.persistence.repository.SalleRepository;

@Service
@Transactional(readOnly = true)
public class DashboardService {

    private final ReservationRepository reservationRepository;
    private final ClientRepository clientRepository;
    private final ChambreRepository chambreRepository;
    private final SalleRepository salleRepository;

    public DashboardService(
            ReservationRepository reservationRepository,
            ClientRepository clientRepository,
            ChambreRepository chambreRepository,
            SalleRepository salleRepository) {

        this.reservationRepository = reservationRepository;
        this.clientRepository = clientRepository;
        this.chambreRepository = chambreRepository;
        this.salleRepository = salleRepository;
    }

    public DashboardDto obtenirStatistiques() {
        return new DashboardDto(
                reservationRepository.count(),
                clientRepository.count(),
                chambreRepository.count(),
                salleRepository.count()
        );
    }
}