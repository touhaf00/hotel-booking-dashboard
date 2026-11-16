package fr.ulco.hotelbooking.controller.api;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import com.jayway.jsonpath.JsonPath;

import fr.ulco.hotelbooking.persistence.entity.Compte;
import fr.ulco.hotelbooking.persistence.entity.Role;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthentificationRestControllerIT {

    private static final String EMAIL =
            "jwt-test@example.invalid";

    // Identifiant fictif, uniquement utilisé dans la base H2 des tests.
    private static final String MOT_DE_PASSE_TEST =
            "MotDePasseTest-2026!";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    void permetAccesApiAvecUnVraiJwtAdministrateur()
            throws Exception {

        creerCompte("ADMIN");

        String jeton = obtenirJeton();

        mockMvc.perform(
                get("/api/dashboard")
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                "Bearer " + jeton
                        )
                        .accept(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.nombreReservations").value(0)
                );
    }

    @Test
    void refuseUnMotDePasseIncorrect() throws Exception {
        creerCompte("ADMIN");

        connecter("MotDePasseIncorrect")
                .andExpect(status().isUnauthorized());
    }

    @Test
    void refuseAccesApiSansJeton() throws Exception {
        mockMvc.perform(
                get("/api/dashboard")
                        .accept(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isUnauthorized());
    }

    @Test
    void refuseUnJetonInvalide() throws Exception {
        mockMvc.perform(
                get("/api/dashboard")
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                "Bearer invalide"
                        )
                        .accept(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isUnauthorized());
    }

    @Test
    void refuseAccesApiSansRoleAdministrateur()
            throws Exception {

        creerCompte("USER");

        String jeton = obtenirJeton();

        mockMvc.perform(
                get("/api/dashboard")
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                "Bearer " + jeton
                        )
                        .accept(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isForbidden());
    }

    @Test
    void refuseUneConnexionSansEmail() throws Exception {
        mockMvc.perform(
                post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "motDePasse": "MotDePasseTest-2026!"
                                }
                                """)
        )
                .andExpect(status().isBadRequest());
    }

    @Test
    void sessionWebPermetDashboardMaisPasApi()
            throws Exception {

        creerCompte("ADMIN");

        MvcResult resultat = mockMvc.perform(
                formLogin()
                        .user(EMAIL)
                        .password(MOT_DE_PASSE_TEST)
        )
                .andExpect(status().isFound())
                .andExpect(authenticated().withUsername(EMAIL))
                .andReturn();

        MockHttpSession session =
                (MockHttpSession) resultat.getRequest()
                        .getSession(false);

        assertNotNull(session);

        mockMvc.perform(
                get("/dashboard").session(session)
        )
                .andExpect(status().isOk());

        mockMvc.perform(
                get("/api/dashboard")
                        .session(session)
                        .accept(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isUnauthorized());
    }

    private void creerCompte(String nomRole) {
        Role role = new Role(nomRole);
        entityManager.persist(role);

        Compte compte = new Compte();
        compte.setNom("Compte JWT de test");
        compte.setEmail(EMAIL);
        compte.setMotDePasse(
                passwordEncoder.encode(MOT_DE_PASSE_TEST)
        );
        compte.setRole(role);

        entityManager.persist(compte);
        entityManager.flush();
        entityManager.clear();
    }

    private ResultActions connecter(String motDePasse)
            throws Exception {

        return mockMvc.perform(
                post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "motDePasse": "%s"
                                }
                                """.formatted(EMAIL, motDePasse))
        );
    }

    private String obtenirJeton() throws Exception {
        MvcResult resultat = connecter(MOT_DE_PASSE_TEST)
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.accessToken").isNotEmpty()
                )
                .andExpect(
                        jsonPath("$.tokenType").value("Bearer")
                )
                .andExpect(
                        jsonPath("$.expiresIn").value(900)
                )
                .andExpect(cookie().doesNotExist("JSESSIONID"))
                .andReturn();

        return JsonPath.read(
                resultat.getResponse()
                        .getContentAsString(StandardCharsets.UTF_8),
                "$.accessToken"
        );
    }
}