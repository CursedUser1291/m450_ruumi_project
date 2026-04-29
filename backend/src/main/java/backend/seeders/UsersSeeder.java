package backend.seeders;

import backend.models.User;
import backend.repositories.UserRepository;
import backend.utils.PasswordHasher;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Order(1)
@Profile("!test")
@Component
public class UsersSeeder implements CommandLineRunner {
    private final UserRepository userRepository;

    public UsersSeeder(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() == 0) {
            User user1 = new User(UUID.randomUUID(), "user1", PasswordHasher.hashPassword("pw1"));
            User user2 = new User(UUID.randomUUID(), "user2", PasswordHasher.hashPassword("pw2"));
            User user3 = new User(UUID.randomUUID(), "user3", PasswordHasher.hashPassword("pw3"));

            userRepository.save(user1);
            userRepository.save(user2);
            userRepository.save(user3);
        }
    }
}