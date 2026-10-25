package com.bms.bms_core.repositories;

import com.bms.bms_core.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    /** Finds a user account by email. */
    public Optional<User> findByEmail(String email);

    /** Retrieves only the stored password hash for authentication. */
    @Query("select u.password from User u where u.email = ?1")
    Optional<String> findPasswordByEmail(String email);

    /** Checks whether the specified user ID belongs to the supplied email. */
    @Query("select count(u) > 0 from User u where u.sysId = ?1 and u.email = ?2")
    boolean existsBySysIdAndEmail(UUID sysId, String email);
}
