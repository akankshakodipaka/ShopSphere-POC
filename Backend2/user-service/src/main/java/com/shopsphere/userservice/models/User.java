package com.shopsphere.userservice.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/** One account: a customer, an admin or the super admin. */
@Entity
@Table(name = "users")   // "user" is a reserved word in some databases, so the table is "users"
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    // the login name; stored in lower case so "A@x.com" and "a@x.com" are the same account
    @Column(nullable = false, unique = true, length = 150)
    private String email;

    // the BCrypt hash, never the real password (about 60 characters long)
    @Column(nullable = false, length = 100)
    private String passwordHash;

    @Column(length = 20)
    private String phone;

    // stored as text ("ADMIN"), not as a number, so reordering the enum can never change old rows
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    // false = blocked: the user cannot log in. Rows are never deleted.
    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    protected User() {}   // JPA needs a no-argument constructor

    public User(String name, String email, String passwordHash, String phone, Role role) {
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.phone = phone;
        this.role = role;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public Role getRole() { return role; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public Instant getCreatedAt() { return createdAt; }
}
