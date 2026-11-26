package com.example.orderservice.Dto;

import java.math.BigDecimal;

public record ItemResponse(
        Long productId,
        String productName,
        BigDecimal price,
        int quantity,
        BigDecimal subtotal) {}