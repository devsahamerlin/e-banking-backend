package com.merlin.digitalbanking.ebankingbackend.dto;

import com.merlin.digitalbanking.ebankingbackend.enums.UserRole;

public record CreateUserDTO(
        String username,
        String password,
        String email,
        String firstName,
        String lastName,
        UserRole role
) {}
