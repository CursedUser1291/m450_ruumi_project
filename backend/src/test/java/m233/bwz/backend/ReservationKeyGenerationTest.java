package m233.bwz.backend;

import backend.models.Reservation;
import backend.repositories.ReservationRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TDD — Public/Private Key Generierung
 *
 * RED:   generateKeys(), findByPublicKey() und validatePrivateKey() fehlten.
 *        Keys werden in die echte Docker-MySQL-DB gespeichert und wieder gelesen.
 *        Alle Tests schlugen fehl.
 *
 * GREEN: ReservationKeyService implementiert:
 *        - generateKeys() erzeugt UUID-Paare und speichert sie in der DB
 *        - findByPublicKey() liest aus DB
 *        - validatePrivateKey() vergleicht mit DB-Wert
 *
 * REFACTOR: @Transactional rollt alle DB-Änderungen nach jedem Test zurück —
 *           Tests sind vollständig isoliert ohne manuelle Cleanup-Logik.
 */
@Transactional
class ReservationKeyGenerationTest extends BaseIntegrationTests {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ReservationKeyService keyService;

    @Test
    void shouldGenerateUniqueKeysForEachReservation() {
        // ARRANGE
        // RED: generateKeys() existiert nicht → kompiliert nicht
        // GREEN: Methode erstellt UUID-Paare und speichert sie via Repository in MySQL
        Reservation r1 = new Reservation();
        Reservation r2 = new Reservation();
        reservationRepository.save(r1);
        reservationRepository.save(r2);

        // ACT
        keyService.generateKeys(r1);
        keyService.generateKeys(r2);

        // ASSERT
        assertNotNull(r1.getPublicKey(),  "PublicKey darf nicht null sein");
        assertNotNull(r1.getPrivateKey(), "PrivateKey darf nicht null sein");
        assertNotEquals(r1.getPublicKey(),  r2.getPublicKey(),
                "Jede Reservierung muss einen eindeutigen PublicKey haben");
        // REFACTOR: Auch PrivateKey-Eindeutigkeit sicherstellen
        assertNotEquals(r1.getPrivateKey(), r2.getPrivateKey(),
                "Jede Reservierung muss einen eindeutigen PrivateKey haben");
    }

    @Test
    void shouldReturnReservationByPublicKey() {
        // ARRANGE
        // RED: findByPublicKey() im Service und Repository fehlt → schlägt fehl
        // GREEN: Repository-Methode findByPublicKey() ergänzt, Service delegiert daran
        Reservation reservation = new Reservation();
        keyService.generateKeys(reservation);
        reservationRepository.save(reservation);
        String publicKey = reservation.getPublicKey();

        // ACT
        Reservation found = keyService.findByPublicKey(publicKey);

        // ASSERT
        assertNotNull(found, "Reservierung muss anhand des PublicKey gefunden werden");
        assertEquals(publicKey, found.getPublicKey());
    }

    @Test
    void shouldThrowWhenPrivateKeyIsInvalid() {
        // ARRANGE
        // RED: validatePrivateKey() fehlt → schlägt fehl
        // GREEN: Methode liest PrivateKey aus DB und vergleicht — falsch → SecurityException
        // REFACTOR: SecurityException statt IllegalArgumentException — semantisch korrekter
        Reservation reservation = new Reservation();
        keyService.generateKeys(reservation);
        reservationRepository.save(reservation);

        // ACT & ASSERT
        assertThrows(SecurityException.class, () ->
                        keyService.validatePrivateKey(reservation, "falscher-key"),
                "Ungültiger PrivateKey muss SecurityException auslösen"
        );
    }
}