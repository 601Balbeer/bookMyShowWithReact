package com.bms.bms_core.schedulers;

import com.bms.bms_core.config.Constants;
import com.bms.bms_core.models.Admin;
import com.bms.bms_core.repositories.AdminRepository;
import com.bms.bms_core.repositories.UserRepository;
import com.bms.bms_core.utilities.CommonUtility;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
public class SystemScheduler {
    private final AdminRepository adminRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String email;
    private final String password;

    public SystemScheduler(
            AdminRepository adminRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            @Value("${security.bootstrap-admin.email:}") String email,
            @Value("${security.bootstrap-admin.password:}") String password) {
        this.adminRepository = adminRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.email = email == null ? "" : email.trim().toLowerCase();
        this.password = password == null ? "" : password;
    }

    /** Creates the bootstrap admin account at application startup when configured and not already present. */
    @EventListener(ApplicationReadyEvent.class)
    public void executeOnce() {
        if (email.isBlank() && password.isBlank()) {
            log.warn("Admin bootstrap skipped: configure BMS_ADMIN_EMAIL and BMS_ADMIN_PASSWORD to create an admin account");
            return;
        }
        if (email.isBlank() || password.length() < 12) {
            throw new IllegalStateException("Configure both BMS_ADMIN_EMAIL and a BMS_ADMIN_PASSWORD of at least 12 characters");
        }
        if (adminRepository.existsByEmail(email)) {
            return;
        }
        if (userRepository.findByEmail(email).isPresent()) {
            throw new IllegalStateException("The configured admin email is already used by a non-admin account");
        }

        LocalDateTime now = LocalDateTime.now();
        Admin admin = Admin.builder()
                .id(CommonUtility.generateUserId("ADMIN"))
                .fullName("BMS Administrator")
                .email(email)
                .password(passwordEncoder.encode(password))
                .createdAt(now)
                .updatedAt(now)
                .createdBy(Constants.SYSTEM_USER_NAME)
                .updatedBy(Constants.SYSTEM_USER_NAME)
                .build();
        adminRepository.save(admin);
        log.info("Bootstrap admin account created for {}", email);
    }
}
