package com.merlin.digitalbanking.ebankingbackend.dto;

public record ChangePasswordDTO(
        String currentPassword,
        String newPassword,
        String confirmPassword
) {}
