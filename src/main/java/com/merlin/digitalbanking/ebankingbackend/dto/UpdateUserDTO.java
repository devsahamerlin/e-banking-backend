package com.merlin.digitalbanking.ebankingbackend.dto;

import com.merlin.digitalbanking.ebankingbackend.enums.UserRole;

public record UpdateUserDTO(
        Long id,
        String email,
        String firstName,
        String lastName,
        UserRole role,
        Boolean isActive
) {}
