package com.bms.bms_core.repositories;

import com.bms.bms_core.models.Admin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AdminRepository extends JpaRepository<Admin, UUID> {
    /** Checks whether an admin account exists for the email. */
    boolean existsByEmail(String email);

    /** Finds the admin account registered with the email. */
    Optional<Admin> findByEmail(String email);
}
