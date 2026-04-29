package backend.seeders;

import backend.models.Reservation;
import backend.models.Room;
import backend.models.User;
import backend.repositories.ReservationRepository;
import backend.repositories.RoomRepository;
import backend.repositories.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.sql.Time;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Order(2)
@Profile("!test")
@Component
public class ReservationsSeeder implements CommandLineRunner {
    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final RoomRepository roomRepository;

    public ReservationsSeeder(ReservationRepository reservationRepository, UserRepository userRepository, RoomRepository roomRepository) {
        this.reservationRepository = reservationRepository;
        this.userRepository = userRepository;
        this.roomRepository = roomRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (reservationRepository.count() == 0) {
            Optional<User> user1 = userRepository.findAll().stream().findFirst();
            Optional<User> user2 = userRepository.findAll().stream().skip(1).findFirst();
            Optional<Room> room1 = roomRepository.findAll().stream().findFirst();
            Optional<Room> room2 = roomRepository.findAll().stream().skip(1).findFirst();
            Optional<Room> room3 = roomRepository.findAll().stream().skip(2).findFirst();

            if (user1.isPresent() && user2.isPresent() && room1.isPresent() && room2.isPresent()) {
                Reservation reservation1 = new Reservation(
                        UUID.randomUUID(),
                        user1.get(),
                        room1.get(),
                        LocalDate.now(),
                        Time.valueOf("10:00:00"),
                        Time.valueOf("12:00:00"),
                        "Team meeting",
                        "Jonas, Ken",
                        UUID.randomUUID(),
                        UUID.randomUUID()
                );

                Reservation reservation2 = new Reservation(
                        UUID.randomUUID(),
                        user2.get(),
                        room2.get(),
                        LocalDate.now().plusDays(1),
                        Time.valueOf("14:00:00"),
                        Time.valueOf("16:00:00"),
                        "Project discussion",
                        "Dario, Chandra, Natalia",
                        UUID.randomUUID(),
                        UUID.randomUUID()
                );

                Reservation reservation3 = new Reservation(
                        UUID.randomUUID(),
                        user2.get(),
                        room3.get(),
                        LocalDate.now().plusDays(1),
                        Time.valueOf("14:00:00"),
                        Time.valueOf("16:00:00"),
                        "Team discussion",
                        "Tom, Jerry, Smithson",
                        UUID.randomUUID(),
                        UUID.randomUUID()
                );

                reservationRepository.save(reservation1);
                reservationRepository.save(reservation2);
                reservationRepository.save(reservation3);
            }
        }
    }
}