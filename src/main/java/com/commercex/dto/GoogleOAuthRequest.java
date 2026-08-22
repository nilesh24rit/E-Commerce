package com.commercex.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Google OAuth2 Authentication Request Payload")
public class GoogleOAuthRequest {

    @Schema(description = "Google ID Token / Credential", example = "eyJhbGciOiJSUzI1NiIsImtpZCI6...")
    private String idToken;

    @NotBlank(message = "Email is required")
    @Email(message = "Valid email address is required")
    @Schema(description = "User's Google Account Email", example = "john.doe@gmail.com")
    private String email;

    @Schema(description = "First name from Google profile", example = "John")
    private String firstName;

    @Schema(description = "Last name from Google profile", example = "Doe")
    private String lastName;

    @Schema(description = "Profile avatar URL", example = "https://lh3.googleusercontent.com/...")
    private String pictureUrl;
}
