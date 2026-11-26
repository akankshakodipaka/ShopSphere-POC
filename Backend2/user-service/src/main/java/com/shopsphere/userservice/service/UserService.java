package com.shopsphere.userservice.service;

import com.shopsphere.userservice.dto.Dtos.ChangePasswordRequest;
import com.shopsphere.userservice.dto.Dtos.CreateUserRequest;
import com.shopsphere.userservice.dto.Dtos.LoginRequest;
import com.shopsphere.userservice.dto.Dtos.LoginResponse;
import com.shopsphere.userservice.dto.Dtos.UpdateProfileRequest;
import com.shopsphere.userservice.dto.Dtos.UserResponse;
import com.shopsphere.userservice.exception.UserException;
import com.shopsphere.userservice.models.Role;
import com.shopsphere.userservice.models.User;
import com.shopsphere.userservice.repository.UserRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Accounts: register, create admins, login, profile, password and blocking. */
@Service
@Transactional
public class UserService {

    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public UserService(UserRepository users, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    // ---------- sign up ----------

    /** Public sign-up. The role is always CUSTOMER, whatever the caller sends. */
    public UserResponse register(CreateUserRequest request) {
        return toResponse(create(request, Role.CUSTOMER));
    }

    /** Only the SUPER_ADMIN reaches this (the controller checks). The new account is an ADMIN. */
    public UserResponse createAdmin(CreateUserRequest request) {
        return toResponse(create(request, Role.ADMIN));
    }

    // ---------- login ----------

    /**
     * Checks email and password and returns a token.
     * An unknown email and a wrong password give the same message, so nobody can use login
     * to find out which emails are registered.
     */
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        User user = users.findByEmail(normalize(request.email()))
                .filter(u -> passwordEncoder.matches(request.password(), u.getPasswordHash()))
                .orElseThrow(() -> new UserException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));
        if (!user.isActive()) {
            throw new UserException(HttpStatus.FORBIDDEN, "This account is blocked");
        }
        return new LoginResponse(jwtService.createToken(user), "Bearer",
                jwtService.expiresInSeconds(), toResponse(user));
    }

    // ---------- my own account ----------

    @Transactional(readOnly = true)
    public UserResponse me(Long userId) {
        return toResponse(find(userId));
    }

    public UserResponse updateMe(Long userId, UpdateProfileRequest request) {
        User user = find(userId);
        user.setName(request.name().trim());
        user.setPhone(request.phone());
        return toResponse(users.save(user));
    }

    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = find(userId);
        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            // 400, not 401: the token is fine, only the typed password is wrong
            throw new UserException(HttpStatus.BAD_REQUEST, "Current password is wrong");
        }
        if (request.currentPassword().equals(request.newPassword())) {
            throw new UserException(HttpStatus.BAD_REQUEST, "New password must be different");
        }
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        users.save(user);
    }

    // ---------- admin screens ----------

    /** All users, or only one role when role is given. */
    @Transactional(readOnly = true)
    public List<UserResponse> list(Role role) {
        List<User> found = role == null ? users.findAllByOrderByNameAsc()
                                        : users.findByRoleOrderByNameAsc(role);
        return found.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public UserResponse get(Long id) {
        return toResponse(find(id));
    }

    /**
     * Block or unblock a user. Rules:
     *  - the SUPER_ADMIN can never be blocked
     *  - nobody changes their own status
     *  - an ADMIN can only be changed by the SUPER_ADMIN
     *  - customers can be changed by any admin
     */
    public UserResponse setActive(Long actorId, Role actorRole, Long targetId, boolean active) {
        User target = find(targetId);
        if (target.getRole() == Role.SUPER_ADMIN) {
            throw new UserException(HttpStatus.FORBIDDEN, "The super admin cannot be blocked");
        }
        if (target.getId().equals(actorId)) {
            throw new UserException(HttpStatus.FORBIDDEN, "You cannot change your own status");
        }
        if (target.getRole() == Role.ADMIN && actorRole != Role.SUPER_ADMIN) {
            throw new UserException(HttpStatus.FORBIDDEN, "Only the super admin can change an admin");
        }
        target.setActive(active);
        return toResponse(users.save(target));
    }

    // ---------- helpers ----------

    // shared by register, createAdmin and the super admin seeder
    public User create(CreateUserRequest request, Role role) {
        String email = normalize(request.email());
        if (users.existsByEmail(email)) {
            throw new UserException(HttpStatus.CONFLICT, "That email is already registered");
        }
        User user = new User(request.name().trim(), email,
                passwordEncoder.encode(request.password()), request.phone(), role);
        return users.save(user);
    }

    private User find(Long id) {
        return users.findById(id)
                .orElseThrow(() -> new UserException(HttpStatus.NOT_FOUND, "User " + id + " not found"));
    }

    // emails are stored and searched in lower case
    private String normalize(String email) {
        return email.trim().toLowerCase();
    }

    // entity -> response, kept private inside the service; the hash never leaves here
    private UserResponse toResponse(User u) {
        return new UserResponse(u.getId(), u.getName(), u.getEmail(), u.getPhone(),
                u.getRole(), u.isActive(), u.getCreatedAt());
    }
}
