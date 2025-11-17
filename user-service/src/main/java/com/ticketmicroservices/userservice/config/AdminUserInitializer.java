package com.ticketmicroservices.userservice.config;

import com.ticketmicroservices.userservice.entity.User;
import com.ticketmicroservices.userservice.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Component
public class AdminUserInitializer {

    private static final Logger logger = LoggerFactory.getLogger(AdminUserInitializer.class);

    @Value("${admin.username:}")
    private String adminUsername;

    @Value("${admin.email:}")
    private String adminEmail;

    @Value("${admin.password:}")
    private String adminPassword;

    @Value("${admin.full-name:Administrator}")
    private String adminFullName;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminUserInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void createAdminOnStartup() {
        if (!StringUtils.hasText(adminUsername) || !StringUtils.hasText(adminEmail) || !StringUtils.hasText(adminPassword)) {
            logger.warn("Admin user configuration missing (username/email/password). Skipping admin user creation.");
            return;
        }

        boolean usernameExists = userRepository.existsByUsername(adminUsername);
        boolean emailExists = userRepository.existsByEmail(adminEmail);

        if (usernameExists || emailExists) {
            logger.info("Admin user already present (usernameExists={}, emailExists={}). Skipping creation.", usernameExists, emailExists);
            return;
        }

        User adminUser = new User(
            adminUsername,
            adminEmail,
            passwordEncoder.encode(adminPassword),
            adminFullName,
            User.Role.ADMIN
        );

        adminUser.setPhoneNumber(null);
        userRepository.save(adminUser);
        logger.info("Admin user created successfully with username='{}' and email='{}'", adminUsername, adminEmail);
    }
}
