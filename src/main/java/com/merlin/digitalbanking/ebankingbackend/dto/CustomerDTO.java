package com.merlin.digitalbanking.ebankingbackend.dto;

import java.time.LocalDateTime;

public record CustomerDTO(Long id, String name, String email,
                          LocalDateTime createdAt,
                          LocalDateTime updatedAt,
                          UserDTO createdBy,
                          UserDTO updatedBy) {}
