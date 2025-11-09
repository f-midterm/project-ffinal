package apartment.example.backend;

import apartment.example.backend.entity.Unit;
import apartment.example.backend.entity.User;
import apartment.example.backend.repository.UnitRepository;
import apartment.example.backend.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.password.PasswordEncoder;
import apartment.example.backend.entity.enums.UnitStatus;
import java.math.BigDecimal;
import java.util.stream.IntStream;

@EnableScheduling
@SpringBootApplication
public class BackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }

    @Bean
    public CommandLineRunner initDatabase(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            UnitRepository unitRepository) {

        return args -> {
            // --- Delete existing users and create new ones with proper BCrypt encoding ---
            userRepository.findByUsername("admin").forEach(userRepository::delete);
            userRepository.findByUsername("testuser").forEach(userRepository::delete);
            userRepository.findByUsername("villager").forEach(userRepository::delete);
            
            // Create admin user
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setEmail("admin@apartment.com");
            admin.setRole(User.Role.ADMIN);
            userRepository.save(admin);
            System.out.println(">>> Created admin user (username: admin, password: admin123)");

            // Create villager user for testing
            User villager = new User();
            villager.setUsername("villager");
            villager.setPassword(passwordEncoder.encode("villager123"));
            villager.setEmail("villager@apartment.com");
            villager.setRole(User.Role.VILLAGER);
            userRepository.save(villager);
            System.out.println(">>> Created villager user (username: villager, password: villager123)");

            // Create test user (different from 'user' to allow registration testing)
            User testUser = new User();
            testUser.setUsername("testuser");
            testUser.setPassword(passwordEncoder.encode("test123"));
            testUser.setEmail("testuser@apartment.com");
            testUser.setRole(User.Role.USER);
            userRepository.save(testUser);
            System.out.println(">>> Created test user (username: testuser, password: test123)");
        };
    }
}