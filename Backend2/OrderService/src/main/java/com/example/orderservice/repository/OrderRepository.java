package com.example.orderservice.repository;
import java.util.List;

import com.example.orderservice.models.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {

    // order history of one customer, newest first
    List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);

    // every order, newest first (for admins)
    List<Order> findAllByOrderByCreatedAtDesc();
}
