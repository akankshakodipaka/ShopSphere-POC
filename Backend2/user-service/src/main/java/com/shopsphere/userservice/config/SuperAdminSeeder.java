package com.shopsphere.userservice.config;

import com.shopsphere.userservice.dto.Dtos.CreateUserRequest;
import com.shopsphere.userservice.models.Role;
import com.shopsphere.userservice.repository.UserRepository;
import com.shopsphere.userservice.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/** Creates the first SUPER_ADMIN at startup if it does not exist yet. */
@Component
public class SuperAdminSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(SuperAdminSeeder.class);

    private final UserRepository users;
    private final UserService userService;
    private final String name;
    private final String email;
    private final String password;

    public SuperAdminSeeder(UserRepository users, UserService userService,
                            @Value("${app.super-admin.name}") String name,
                            @Value("${app.super-admin.email}") String email,
                            @Value("${app.super-admin.password}") String password) {
        this.users = users;
        this.userService = userService;
        this.name = name;
        this.email = email.trim().toLowerCase();
        this.password = password;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (users.existsByEmail(email)) {
            return;   // already there, leave it alone
        }
        // same method as register, so the password is hashed and the email lower-cased the same way
        userService.create(new CreateUserRequest(name, email, password, null), Role.SUPER_ADMIN);
        log.info("Created the first super admin: {}", email);
    }
}
