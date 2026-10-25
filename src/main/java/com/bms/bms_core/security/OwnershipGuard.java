package com.bms.bms_core.security;

import com.bms.bms_core.repositories.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class OwnershipGuard {
    private final UserRepository userRepository;

    public OwnershipGuard(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /** Checks whether the authenticated principal matches the requested user ID. */
    public boolean isCurrentUser(Authentication authentication, UUID userId) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        boolean isTheaterOwner = authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_THEATER_OWNER".equals(authority.getAuthority()));
        return isTheaterOwner
                && userRepository.existsBySysIdAndEmail(userId, authentication.getName());
    }
}
