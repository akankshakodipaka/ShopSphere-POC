package com.shopsphere.userservice.repository;

import com.shopsphere.userservice.models.Role;
import com.shopsphere.userservice.models.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    // used by login: find the account for an email
    Optional<User> findByEmail(String email);

    // used by register, create-admin and the super admin seeder: is the email already taken?
    boolean existsByEmail(String email);

    // admin screens: all users, or only one role, sorted by name
    List<User> findAllByOrderByNameAsc();

    List<User> findByRoleOrderByNameAsc(Role role);
}
