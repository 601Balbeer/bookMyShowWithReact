package com.bms.bms_core.repositories;

import com.bms.bms_core.models.TheatreOwner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TheaterOwnerRepository extends JpaRepository<TheatreOwner, UUID> {
    /** Checks whether a theater-owner account exists for the email. */
    boolean existsByEmail(String email);
}
