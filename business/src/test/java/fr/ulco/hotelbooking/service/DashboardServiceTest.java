package fr.ulco.hotelbooking.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import fr.ulco.hotelbooking.dto.DashboardDto;
import fr.ulco.hotelbooking.persistence.repository.ChambreRepository;
import fr.ulco.hotelbooking.persistence.repository.ClientRepository;
import fr.ulco.hotelbooking.persistence.repository.ReservationRepository;
import fr.ulco.hotelbooking.persistence.repository.SalleRepository;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private ChambreRepository chambreRepository;

    @Mock
    private SalleRepository salleRepository;

    @InjectMocks
    private DashboardService dashboardService;

    @Test
    void testRetourneLesQuatreCompteurs() {
        // Arrange : préparer les valeurs retournées par les repositories.
        simulerCompteurs(6, 2, 1, 3);

        // Act : appeler le service réel.
        DashboardDto resultat = dashboardService.obtenirStatistiques();

        // Assert : vérifier les quatre valeurs et leur ordre.
        assertEquals(new DashboardDto(6, 2, 1, 3), resultat);
    }

    @Test
    void testRetourneDesCompteursVides() {
        simulerCompteurs(0, 0, 0, 0);

        DashboardDto resultat = dashboardService.obtenirStatistiques();

        assertEquals(new DashboardDto(0, 0, 0, 0), resultat);
    }

    @Test
    void testConserveUnGrandNombreDeReservations() {
        long nombreReservations = 3_000_000_000L;

        simulerCompteurs(nombreReservations, 2, 1, 3);

        DashboardDto resultat = dashboardService.obtenirStatistiques();

        assertEquals(
                new DashboardDto(nombreReservations, 2, 1, 3),
                resultat
        );
    }

    private void simulerCompteurs(
            long reservations,
            long clients,
            long chambres,
            long salles) {

        when(reservationRepository.count()).thenReturn(reservations);
        when(clientRepository.count()).thenReturn(clients);
        when(chambreRepository.count()).thenReturn(chambres);
        when(salleRepository.count()).thenReturn(salles);
    }
}