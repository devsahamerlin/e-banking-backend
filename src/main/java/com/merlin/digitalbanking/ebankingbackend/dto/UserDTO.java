package com.merlin.digitalbanking.ebankingbackend.dto;

import com.merlin.digitalbanking.ebankingbackend.enums.UserRole;

import java.time.LocalDateTime;

public record UserDTO(
        Long id,
        String username,
        String email,
        String firstName,
        String lastName,
        UserRole role,
        Boolean isActive,
        LocalDateTime createdAt,
        LocalDateTime lastLogin
) {}
