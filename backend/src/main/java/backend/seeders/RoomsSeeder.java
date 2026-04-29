package backend.seeders;

import backend.models.Room;
import backend.repositories.RoomRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Order(1)
@Profile("!test")
@Component
public class RoomsSeeder implements CommandLineRunner {
    private final RoomRepository roomRepository;

    public RoomsSeeder(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (roomRepository.count() == 0) {
            Room room101 = new Room(UUID.randomUUID(), 101, "Room 101", true, true, false, false);
            Room room102 = new Room(UUID.randomUUID(), 102, "Room 102", false, true, true, false);
            Room room103 = new Room(UUID.randomUUID(), 103, "Room 103", true, false, false, true);
            Room room104 = new Room(UUID.randomUUID(), 104, "Room 104", false, false, true, true);
            Room room105 = new Room(UUID.randomUUID(), 105, "Room 105", true, true, true, true);

            roomRepository.save(room101);
            roomRepository.save(room102);
            roomRepository.save(room103);
            roomRepository.save(room104);
            roomRepository.save(room105);
        }
    }
}