package com.softuni.gearshare.config;

import com.softuni.gearshare.exception.EntityNotFoundException;
import com.softuni.gearshare.model.entity.User;
import com.softuni.gearshare.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CurrentUserProvider {

    private final UserRepository userRepository;

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new EntityNotFoundException("No authenticated user in the current session.");
        }
        AppUserPrincipal principal = (AppUserPrincipal) authentication.getPrincipal();
        return userRepository.findById(principal.getId())
                .orElseThrow(() -> new EntityNotFoundException("Authenticated user no longer exists."));
    }
}
