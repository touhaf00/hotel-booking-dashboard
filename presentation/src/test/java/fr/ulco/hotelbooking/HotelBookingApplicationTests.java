package fr.ulco.hotelbooking;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class HotelBookingApplicationTests {

    @Autowired
    private DataSource dataSource;

    @Test
    void testDemarreAvecLaBaseH2() throws SQLException {
        try (Connection connexion = dataSource.getConnection()) {
            assertEquals(
                    "H2",
                    connexion.getMetaData().getDatabaseProductName()
            );
        }
    }
}