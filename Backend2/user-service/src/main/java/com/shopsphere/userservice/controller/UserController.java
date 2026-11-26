package com.shopsphere.userservice.controller;

import com.shopsphere.userservice.dto.Dtos.ChangePasswordRequest;
import com.shopsphere.userservice.dto.Dtos.CreateUserRequest;
import com.shopsphere.userservice.dto.Dtos.StatusRequest;
import com.shopsphere.userservice.dto.Dtos.UpdateProfileRequest;
import com.shopsphere.userservice.dto.Dtos.UserResponse;
import com.shopsphere.userservice.models.Role;
import com.shopsphere.userservice.service.UserService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Everything here needs a token. Some endpoints also need an admin role. */
@RestController
@RequestMapping("/v1")
public class UserController {

    private static final String ADMIN = "hasAnyRole('ADMIN','SUPER_ADMIN')";
    private static final String SUPER_ADMIN = "hasRole('SUPER_ADMIN')";

    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    // ---------- my own account (any logged-in user) ----------

    // GET /users/me
    @GetMapping("/getUser")
    public UserResponse me(@AuthenticationPrincipal Jwt jwt) {
        return service.me(userId(jwt));
    }

    // PUT /users/me   body: {"name":"Asha K","phone":"9876543210"}
    @PutMapping("/updateUserDetails")
    public UserResponse updateMe(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody UpdateProfileRequest request) {
        return service.updateMe(userId(jwt), request);
    }

    // PUT /users/me/password   body: {"currentPassword":"secret123","newPassword":"newsecret456"}
    @PutMapping("/UpdatePassword")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody ChangePasswordRequest request) {
        service.changePassword(userId(jwt), request);
    }

    // ---------- super admin only ----------

    // POST /users/admins   body: same as register. Creates an ADMIN.
    @PostMapping("/createAdmins")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(SUPER_ADMIN)
    public UserResponse createAdmin(@Valid @RequestBody CreateUserRequest request) {
        return service.createAdmin(request);
    }

    // ---------- admin screens ----------

    // GET /users              all users
    // GET /users?role=ADMIN   only one role (CUSTOMER, ADMIN or SUPER_ADMIN)
    @GetMapping("/getUserDetails")
    @PreAuthorize(ADMIN)
    public List<UserResponse> list(@RequestParam(required = false) Role role) {
        return service.list(role);
    }

    // GET /users/5
    @GetMapping("/getUserDetailsById/{id}")
    @PreAuthorize(ADMIN)
    public UserResponse get(@PathVariable Long id) {
        return service.get(id);
    }

    // PUT /users/5/status   body: {"active":false}   blocks or unblocks (rules are in UserService.setActive)
    @PutMapping("/updateUserStatus/{id}")
    @PreAuthorize(ADMIN)
    public UserResponse setStatus(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id,
                                  @Valid @RequestBody StatusRequest request) {
        return service.setActive(userId(jwt), role(jwt), id, request.active());
    }

    // ---------- who is calling? read from the verified token ----------

    private Long userId(Jwt jwt) {
        return Long.valueOf(jwt.getSubject());
    }

    private Role role(Jwt jwt) {
        return Role.valueOf(jwt.getClaimAsString("role"));
    }
}
