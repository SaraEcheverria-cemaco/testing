package com.discounts.config;

import com.discounts.model.User;
import com.discounts.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    DataLoader(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        // Crear usuarios de prueba
        User regularUser = new User(
                "user1",
                passwordEncoder.encode("password123"),
                false
        );

        User vipUser = new User(
                "vipuser",
                passwordEncoder.encode("password123"),
                true
        );

        userRepository.save(regularUser);
        userRepository.save(vipUser);

        System.out.println("Usuarios de prueba creados:");
        System.out.println("Usuario regular: user1 / password123");
        System.out.println("Usuario VIP: vipuser / password123");
    }
}
