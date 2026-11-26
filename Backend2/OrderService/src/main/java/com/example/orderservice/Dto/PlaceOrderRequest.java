package com.example.orderservice.Dto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record PlaceOrderRequest(
        @NotNull Long userId,
        @NotBlank String shippingName,
        @NotBlank String shippingAddress,
        @NotBlank String phone,
        @NotEmpty @Valid List<ItemRequest> items) {}