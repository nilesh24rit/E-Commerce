package com.commercex.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.UUID;

/**
 * A basic DTO to transfer User information safely,
 * hiding sensitive fields like 'password'.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
    private Set<String> roles; // E.g., ["ROLE_CUSTOMER"]
}
