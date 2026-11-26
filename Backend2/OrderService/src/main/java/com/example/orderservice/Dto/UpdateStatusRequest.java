package com.example.orderservice.Dto;

import com.example.orderservice.models.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateStatusRequest(@NotNull OrderStatus status) {}
