package com.shopsphere.userservice.dto;

import com.shopsphere.userservice.models.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.Instant;

/** Request and response shapes. The User entity is never sent over HTTP. */
public final class Dtos {

    private Dtos() {}   // only groups the records

    // ---------- requests ----------

    /** Body of POST /auth/register (creates a CUSTOMER) and POST /users/admins (creates an ADMIN) */
    public record CreateUserRequest(
            @NotBlank @Size(max = 100) String name,
            @NotBlank @Email @Size(max = 150) String email,
            // 72 is BCrypt's limit: anything longer would be silently cut off
            @NotBlank @Size(min = 8, max = 72) String password,
            // optional; a null phone passes, a given one must look like a phone number
            @Pattern(regexp = "^[0-9+\\-\\s]{7,20}$", message = "must be a valid phone number") String phone) {}

    /** Body of POST /auth/login */
    public record LoginRequest(
            @NotBlank String email,
            @NotBlank String password) {}

    /** Body of PUT /users/me */
    public record UpdateProfileRequest(
            @NotBlank @Size(max = 100) String name,
            @Pattern(regexp = "^[0-9+\\-\\s]{7,20}$", message = "must be a valid phone number") String phone) {}

    /** Body of PUT /users/me/password */
    public record ChangePasswordRequest(
            @NotBlank String currentPassword,
            @NotBlank @Size(min = 8, max = 72) String newPassword) {}

    /** Body of PUT /users/{id}/status. Boolean (not boolean), so a missing field is rejected instead of meaning false. */
    public record StatusRequest(@NotNull Boolean active) {}

    // ---------- responses ----------

    /** A user as other people may see it. There is deliberately no password or hash field. */
    public record UserResponse(Long id, String name, String email, String phone,
                               Role role, boolean active, Instant createdAt) {}

    /** What login returns: the token plus the user, so a screen can show the name without another call. */
    public record LoginResponse(String token, String tokenType, long expiresInSeconds, UserResponse user) {}
}
