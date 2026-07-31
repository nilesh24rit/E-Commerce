package com.commercex.dto;

import lombok.Builder;
import lombok.Data;
import java.util.Set;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse {
    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
    private Set<String> roles;
}
