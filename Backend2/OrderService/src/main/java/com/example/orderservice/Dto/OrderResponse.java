package com.example.orderservice.Dto;

import com.example.orderservice.models.Order;
import com.example.orderservice.models.OrderStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderResponse(
        Long id,
        Long userId,
        OrderStatus status,
        BigDecimal total,
        Instant createdAt,
        String shippingName,
        String shippingAddress,
        String phone,
        List<ItemResponse> items) {

    // converts an Order entity into the response shape
    public static OrderResponse from(Order o) {
        List<ItemResponse> items = o.getItems().stream()
                .map(i -> new ItemResponse(i.getProductId(), i.getProductName(),
                        i.getPrice(), i.getQuantity(), i.subtotal()))
                .toList();
        return new OrderResponse(o.getId(), o.getUserId(), o.getStatus(), o.getTotal(),
                o.getCreatedAt(), o.getShippingName(), o.getShippingAddress(),
                o.getPhone(), items);
    }
}