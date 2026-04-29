package m233.bwz.backend;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TDD — Reservierungsdauer-Validierung
 *
 * RED:   Diese Tests wurden zuerst geschrieben, BEVOR validate() implementiert war.
 *        Die Methode existierte nicht -> Tests schlugen fehl / kompilierten nicht.
 *        Datenbank läuft als echter MySQL-Container via Testcontainers.
 *
 * GREEN: validate() in ReservationValidationService implementiert:
 *        - start in Vergangenheit -> IllegalArgumentException
 *        - Dauer > 8h -> IllegalArgumentException
 *        - 30 Minuten, Zukunft -> kein Fehler
 *
 * REFACTOR: Magic Numbers durch Konstante MAX_DURATION_HOURS = 8 ersetzt.
 *           Fehlermeldungen in eine gemeinsame Klasse ausgelagert.
 */
@Transactional
class ReservationDurationValidationTest extends BaseIntegrationTests {

    @Autowired
    private ReservationValidationService validationService;

    @Test
    void shouldRejectReservationLongerThan8Hours() {
        // ARRANGE
        // RED: validate() existiert nicht → kompiliert nicht
        // GREEN: Prüfung: duration > 8h → IllegalArgumentException
        LocalDateTime start = LocalDateTime.now().plusHours(1);
        LocalDateTime end = start.plusHours(9); // 9 Stunden → ungültig

        // ACT & ASSERT
        assertThrows(IllegalArgumentException.class, () ->
                        validationService.validate(start, end),
                "Reservierung länger als 8 Stunden muss abgelehnt werden"
        );
    }

    @Test
    void shouldRejectReservationInThePast() {
        // ARRANGE
        // RED: Keine Vergangenheits-Prüfung → Test schlägt fehl
        // GREEN: start.isBefore(LocalDateTime.now()) → IllegalArgumentException
        LocalDateTime start = LocalDateTime.now().minusHours(2);
        LocalDateTime end = start.plusHours(1);

        // ACT & ASSERT
        assertThrows(IllegalArgumentException.class, () ->
                        validationService.validate(start, end),
                "Reservierung in der Vergangenheit muss abgelehnt werden"
        );
    }

    @Test
    void shouldAllowReservationOf30Minutes() {
        // ARRANGE
        // RED: validate() fehlt → Test schlägt fehl
        // GREEN: Gültige Eingabe → keine Exception
        // REFACTOR: assertDoesNotThrow macht Absicht klarer als try/catch
        LocalDateTime start = LocalDateTime.now().plusHours(1);
        LocalDateTime end = start.plusMinutes(30);

        // ACT & ASSERT
        assertDoesNotThrow(() ->
                        validationService.validate(start, end),
                "Reservierung von 30 Minuten in der Zukunft muss erlaubt sein"
        );
    }
}