package fr.ulco.hotelbooking.persistence.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ContextConfiguration;

import fr.ulco.hotelbooking.persistence.entity.Chambre;
import fr.ulco.hotelbooking.persistence.entity.Salle;

@DataJpaTest(showSql = false)
@ContextConfiguration(
        classes = ChambreRepositoryIT.ConfigurationJpa.class
)
class ChambreRepositoryIT {

    @Autowired
    private ChambreRepository chambreRepository;

    @Autowired
    private SalleRepository salleRepository;

    @Autowired
    private RessourceRepository ressourceRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    void testEnregistreEtRetrouveUneChambre() {
        // Arrange
        Chambre chambre = creerChambre("TEST-101");

        // Act
        Chambre enregistree = chambreRepository.saveAndFlush(chambre);
        Long id = enregistree.getId();

        assertNotNull(id);

        // Vider le contexte pour relire effectivement depuis la base.
        entityManager.clear();

        Chambre retrouvee = chambreRepository.findById(id)
                .orElseThrow();

        // Assert
        assertEquals("TEST-101", retrouvee.getNumero());
        assertEquals(1, retrouvee.getEtage());
        assertEquals(2, retrouvee.getCapacite());
        assertEquals("Chambre de test", retrouvee.getDescription());
    }

    @Test
    void testRefuseUnNumeroDeChambreDejaUtilise() {
        chambreRepository.saveAndFlush(
                creerChambre("TEST-101")
        );

        Chambre doublon = creerChambre("TEST-101");

        assertThrows(
                DataIntegrityViolationException.class,
                () -> chambreRepository.saveAndFlush(doublon)
        );
    }

    @Test
    void testDistingueLesChambresDesSallesDansLesCompteurs() {
        chambreRepository.save(creerChambre("TEST-201"));
        chambreRepository.save(creerChambre("TEST-202"));

        Salle salle = new Salle();
        salle.setNom("Salle de test");
        salle.setEtage(0);
        salle.setCapacite(20);
        salle.setDescription("Salle de reunion");

        salleRepository.saveAndFlush(salle);
        entityManager.clear();

        assertEquals(2L, chambreRepository.count());
        assertEquals(1L, salleRepository.count());
        assertEquals(3L, ressourceRepository.count());
    }

    private Chambre creerChambre(String numero) {
        Chambre chambre = new Chambre();
        chambre.setNumero(numero);
        chambre.setEtage(1);
        chambre.setCapacite(2);
        chambre.setDescription("Chambre de test");
        return chambre;
    }

    @Configuration(proxyBeanMethods = false)
    @EnableAutoConfiguration
    @EntityScan(basePackageClasses = Chambre.class)
    @EnableJpaRepositories(
            basePackageClasses = ChambreRepository.class
    )
    static class ConfigurationJpa {
    }
}