package com.shopsphere.userservice.controller;

import com.shopsphere.userservice.dto.Dtos.CreateUserRequest;
import com.shopsphere.userservice.dto.Dtos.LoginRequest;
import com.shopsphere.userservice.dto.Dtos.LoginResponse;
import com.shopsphere.userservice.dto.Dtos.UserResponse;
import com.shopsphere.userservice.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Public endpoints: no token needed (SecurityConfig opens /auth/**). */
@RestController
@RequestMapping("/v1")
public class AuthController {

    private final UserService service;

    public AuthController(UserService service) {
        this.service = service;
    }

    // POST /auth/register   body: {"name":"Asha","email":"asha@x.com","password":"secret123","phone":"9876543210"}
    // Always creates a CUSTOMER.
    @PostMapping("/registerUser")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@Valid @RequestBody CreateUserRequest request) {
        return service.register(request);
    }

    // POST /auth/login   body: {"email":"asha@x.com","password":"secret123"}
    // Returns {"token":"...","tokenType":"Bearer","expiresInSeconds":3600,"user":{...}}
    @PostMapping("/loginUser")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return service.login(request);
    }
}
