package m233.bwz.backend;

import backend.models.Reservation;
import backend.repositories.ReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ser.Serializers;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TDD - Konflikt-Erkennung bei Überschneidung
 *
 * RED:   hasConflict() existierte nicht.
 *        Testdaten werden in die echte Docker-MySQL-DB geschrieben (@Transactional rollt zurück).
 *        Tests schlugen fehl - keine Überschneidungslogik vorhanden.
 *
 * GREEN: hasConflict() implementiert:
 *        Überschneidung wenn: existingStart < newEnd AND existingEnd > newStart
 *
 * REFACTOR: @Transactional sorgt dafür dass jeder Test mit sauberer DB startet.
 *           Grenzfall (direkt angrenzend) mit strikter Ungleichheit (<, >) gelöst.
 */
@Transactional
class ReservationConflictDetectionTest extends Serializers.Base {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ReservationConflictService conflictService;

    private Long roomId;

    @BeforeEach
    void setUp() {
        // Bestehende Reservierung in DB schreiben: 10:00 – 12:00
        // RED: Reservation-Entity hat noch keine Felder → kompiliert nicht
        // GREEN: Entity mit start, end, roomId erstellt und gespeichert
        roomId = 1L;
        Reservation existing = new Reservation();
        existing.setRoomId(roomId);
        existing.setStart(LocalDateTime.of(2025, 6, 1, 10, 0));
        existing.setEnd(LocalDateTime.of(2025, 6, 1, 12, 0));
        reservationRepository.save(existing);
    }

    @Test
    void shouldDetectOverlapWhenNewStartIsInsideExisting() {
        // ARRANGE
        // RED: hasConflict() fehlt → schlägt fehl
        // GREEN: Query prüft Überschneidung in DB — newStart liegt in 10–12
        LocalDateTime newStart = LocalDateTime.of(2025, 6, 1, 11, 0);
        LocalDateTime newEnd   = LocalDateTime.of(2025, 6, 1, 13, 0);

        // ACT & ASSERT
        assertTrue(
                conflictService.hasConflict(roomId, newStart, newEnd),
                "Start liegt in bestehender Reservierung → Konflikt erwartet"
        );
    }

    @Test
    void shouldDetectOverlapWhenNewEndIsInsideExisting() {
        // ARRANGE
        // RED: Nur Start wurde geprüft, Ende fehlte → schlägt fehl
        // GREEN: Bedingung erweitert: auch Ende der neuen Reservierung geprüft
        LocalDateTime newStart = LocalDateTime.of(2025, 6, 1, 9, 0);
        LocalDateTime newEnd   = LocalDateTime.of(2025, 6, 1, 11, 0);

        // ACT & ASSERT
        assertTrue(
                conflictService.hasConflict(roomId, newStart, newEnd),
                "Ende liegt in bestehender Reservierung → Konflikt erwartet"
        );
    }

    @Test
    void shouldAllowReservationDirectlyAfterAnother() {
        // ARRANGE
        // RED: Grenzfall — direkt angrenzende Zeit wurde fälschlich als Konflikt erkannt
        // GREEN: Strikte < und > statt <= und >= behebt den Grenzfall
        // REFACTOR: Dieser Test dokumentiert das korrekte Grenzfallverhalten dauerhaft
        LocalDateTime newStart = LocalDateTime.of(2025, 6, 1, 12, 0); // genau bei existingEnd
        LocalDateTime newEnd   = LocalDateTime.of(2025, 6, 1, 14, 0);

        // ACT & ASSERT
        assertFalse(
                conflictService.hasConflict(roomId, newStart, newEnd),
                "Direkt nach einer Reservierung darf kein Konflikt erkannt werden"
        );
    }
}