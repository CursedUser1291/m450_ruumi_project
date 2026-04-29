package m233.bwz.backend;

import backend.models.Reservation;
import backend.models.Room;
import backend.repositories.ReservationRepository;
import backend.repositories.RoomRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TDD — Raum-Verfügbarkeit für einen Zeitraum
 *
 * RED:   getAvailableRooms() existierte nicht.
 *        Räume und Reservierungen werden in die echte Docker-MySQL-DB geschrieben.
 *        Tests schlugen fehl — kein Filter vorhanden.
 *
 * GREEN: getAvailableRooms() implementiert:
 *        Alle Räume laden, dann diejenigen herausfiltern bei denen hasConflict() true ist.
 *
 * REFACTOR: Stream-Filter in isRoomAvailable() extrahiert.
 *           @Transactional sorgt für saubere DB nach jedem Test.
 */
@Transactional
class RoomAvailabilityTest extends BaseIntegrationTests {

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private RoomAvailabilityService availabilityService;

    private LocalDateTime slotStart;
    private LocalDateTime slotEnd;

    @BeforeEach
    void setUp() {
        slotStart = LocalDateTime.of(2025, 6, 1, 10, 0);
        slotEnd   = LocalDateTime.of(2025, 6, 1, 12, 0);

        // RED: Room-Entity existiert noch nicht → kompiliert nicht
        // GREEN: Room mit name-Feld erstellt, in MySQL-Container gespeichert
        Room roomA = new Room("Raum A");
        Room roomB = new Room("Raum B");
        roomRepository.save(roomA);
        roomRepository.save(roomB);

        // Raum B ist für den gesuchten Zeitraum bereits gebucht
        Reservation existingBooking = new Reservation();
        existingBooking.setRoomId(roomB.getId());
        existingBooking.setStart(slotStart);
        existingBooking.setEnd(slotEnd);
        reservationRepository.save(existingBooking);
    }

    @Test
    void shouldReturnOnlyAvailableRoomsForGivenSlot() {
        // RED: getAvailableRooms() fehlt → schlägt fehl
        // GREEN: Methode filtert Raum B heraus, gibt nur Raum A zurück
        List<Room> available = availabilityService.getAvailableRooms(slotStart, slotEnd);

        assertEquals(1, available.size(), "Nur ein Raum soll verfügbar sein");
        assertEquals("Raum A", available.get(0).getName());
    }

    @Test
    void shouldReturnEmptyListWhenAllRoomsBooked() {
        // ARRANGE
        // RED: Null-Rückgabe statt leere Liste → NullPointerException im Test
        // GREEN: Stream.filter().toList() gibt automatisch leere Liste zurück — nie null
        // REFACTOR: assertNotNull + assertTrue isEmpty() ist klarer als assertEquals(0, size())
        Room roomC = new Room("Raum C");
        roomRepository.save(roomC);

        Reservation booking = new Reservation();
        booking.setRoomId(roomC.getId());
        booking.setStart(slotStart);
        booking.setEnd(slotEnd);
        reservationRepository.save(booking);

        // Raum A auch buchen damit alle Räume belegt sind
        Reservation bookingA = new Reservation();
        Room roomA = roomRepository.findByName("Raum A");
        bookingA.setRoomId(roomA.getId());
        bookingA.setStart(slotStart);
        bookingA.setEnd(slotEnd);
        reservationRepository.save(bookingA);

        // ACT
        List<Room> available = availabilityService.getAvailableRooms(slotStart, slotEnd);

        // ASSERT
        assertNotNull(available, "Rückgabe darf nie null sein");
        assertTrue(available.isEmpty(), "Leere Liste erwartet wenn alle Räume gebucht sind");
    }
}