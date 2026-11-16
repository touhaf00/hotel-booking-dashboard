package fr.ulco.hotelbooking.controller.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import fr.ulco.hotelbooking.persistence.entity.Chambre;
import fr.ulco.hotelbooking.persistence.entity.Client;
import fr.ulco.hotelbooking.persistence.entity.Compte;
import fr.ulco.hotelbooking.persistence.entity.Reservation;
import fr.ulco.hotelbooking.persistence.entity.Role;
import fr.ulco.hotelbooking.persistence.entity.Salle;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;

import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@WithMockUser(username = "dashboard-test", roles = "ADMIN")
class DashboardIntegrationIT {

    @Autowired
    private MockMvc mockMvc;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    void testApiRetourneDesZerosQuandLaBaseEstVide() throws Exception {
    	mockMvc.perform(
    	        get("/api/dashboard")
    	                .with(jwt().authorities(
    	                        new SimpleGrantedAuthority("ROLE_ADMIN")
    	                ))
    	                .accept(MediaType.APPLICATION_JSON)
    	)
                .andExpect(status().isOk())
                .andExpect(content()
                        .contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.nombreReservations").value(0))
                .andExpect(jsonPath("$.nombreClients").value(0))
                .andExpect(jsonPath("$.nombreChambres").value(0))
                .andExpect(jsonPath("$.nombreSalles").value(0));
    }

    @Test
    void testApiRetourneLesCompteursDeLaBase() throws Exception {
        creerDonnees();

        mockMvc.perform(
                get("/api/dashboard")
                        .with(jwt().authorities(
                                new SimpleGrantedAuthority("ROLE_ADMIN")
                        ))
                        .accept(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isOk())
                .andExpect(content()
                        .contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.nombreReservations").value(6))
                .andExpect(jsonPath("$.nombreClients").value(2))
                .andExpect(jsonPath("$.nombreChambres").value(1))
                .andExpect(jsonPath("$.nombreSalles").value(3));
    }

    @Test
    void testPageAfficheLesCinqPlusGrandsNumeros() throws Exception {
        List<Long> ids = creerDonnees();

        MvcResult resultat = mockMvc.perform(
                get("/dashboard").accept(MediaType.TEXT_HTML)
        )
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard"))
                .andExpect(content()
                        .contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andReturn();

        String html = HtmlUtils.htmlUnescape(
                resultat.getResponse()
                        .getContentAsString(StandardCharsets.UTF_8)
        );

        assertThat(html)
                .containsSubsequence(
                        ">N° " + ids.get(5) + "<",
                        ">N° " + ids.get(4) + "<",
                        ">N° " + ids.get(3) + "<",
                        ">N° " + ids.get(2) + "<",
                        ">N° " + ids.get(1) + "<"
                )
                .doesNotContain(">N° " + ids.get(0) + "<")
                .contains(
                        "<html",
                        "16/11/2026",
                        "18/11/2026",
                        "120,00",
                        "Confirmée",
                        "Annulée",
                        "En attente"
                );
    }

    @Test
    void testAjaxRetourneUniquementLeFragmentAvecUnEtatVide()
            throws Exception {

        MvcResult resultat = mockMvc.perform(
                get("/dashboard/contenu").accept(MediaType.TEXT_HTML)
        )
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard :: contenu"))
                .andExpect(content()
                        .contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andReturn();

        String html = HtmlUtils.htmlUnescape(
                resultat.getResponse()
                        .getContentAsString(StandardCharsets.UTF_8)
        );

        assertThat(html)
                .contains(
                        "id=\"dashboard-contenu\"",
                        "Aucune réservation pour le moment"
                )
                .doesNotContain("<html", "<head", "<body");
    }

    private List<Long> creerDonnees() {
        Role role = new Role();
        role.setNom("TEST");
        entityManager.persist(role);

        Compte compte = new Compte();
        compte.setNom("Compte de test");
        compte.setEmail("dashboard-test@example.invalid");
        compte.setMotDePasse("non-utilise-dans-ces-tests");
        compte.setRole(role);
        entityManager.persist(compte);

        List<Client> clients = new ArrayList<>();

        for (int i = 0; i < 2; i++) {
            Client client = new Client();
            client.setNom("Client de test " + i);
            client.setPrenom("Alex");
            client.setCompte(compte);
            entityManager.persist(client);
            clients.add(client);
        }

        Chambre chambre = new Chambre();
        chambre.setNumero("TEST-101");
        chambre.setEtage(1);
        chambre.setCapacite(2);
        chambre.setDescription("Chambre de test");
        entityManager.persist(chambre);

        for (int i = 0; i < 3; i++) {
            Salle salle = new Salle();
            salle.setNom("Salle de test " + i);
            salle.setEtage(0);
            salle.setCapacite(20);
            entityManager.persist(salle);
        }

        List<Reservation> reservations = new ArrayList<>();
        String[] statuts = {"CONFIRMEE", "ANNULEE", "EN_ATTENTE"};
        LocalDate premiereDate = LocalDate.of(2026, 11, 1);

        for (int i = 0; i < 6; i++) {
            LocalDate debut = premiereDate.plusDays(i * 3L);

            Reservation reservation = new Reservation();
            reservation.setDateDebut(debut);
            reservation.setDateFin(debut.plusDays(2));
            reservation.setPrixSejour(new BigDecimal("120.00"));
            reservation.setStatut(statuts[i % statuts.length]);
            reservation.setClient(clients.get(i % clients.size()));
            reservation.setCompte(compte);
            reservation.setRessource(chambre);

            entityManager.persist(reservation);
            reservations.add(reservation);
        }

        entityManager.flush();

        List<Long> ids = reservations.stream()
                .map(Reservation::getId)
                .toList();

        entityManager.clear();

        return ids;
    }
    
    @Test
    @WithAnonymousUser
    void testRedirigeVersConnexionSansAuthentification()
            throws Exception {

        mockMvc.perform(get("/dashboard"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    @WithMockUser(username = "utilisateur-test", roles = "USER")
    void testRefuseLeDashboardSansRoleAdministrateur()
            throws Exception {

        mockMvc.perform(get("/dashboard"))
                .andExpect(status().isForbidden());
    }
}