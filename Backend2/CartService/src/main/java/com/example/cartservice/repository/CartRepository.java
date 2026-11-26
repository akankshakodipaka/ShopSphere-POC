package com.example.cartservice.repository;
import com.example.cartservice.models.Cart;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartRepository extends JpaRepository<Cart, Long> {

    // the one cart of a user, if they have started one
    Optional<Cart> findByUserId(Long userId);
}
