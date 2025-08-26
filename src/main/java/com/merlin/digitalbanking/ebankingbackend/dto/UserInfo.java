package com.merlin.digitalbanking.ebankingbackend.dto;

import com.merlin.digitalbanking.ebankingbackend.enums.UserRole;

public record UserInfo(
        Long id,
        String username,
        String email,
        String firstName,
        String lastName,
        UserRole role
) {}