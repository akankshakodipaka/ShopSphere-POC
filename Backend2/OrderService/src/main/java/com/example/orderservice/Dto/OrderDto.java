package com.example.orderservice.Dto;
import com.example.orderservice.models.Order;
import com.example.orderservice.models.OrderStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

    /** Request and response shapes. The JPA entities are never sent over HTTP. */
    public  class OrderDto {

        private OrderDto() {}

        // ---- incoming ----
        public record ItemRequest(
                @NotNull Long productId,
                @NotBlank String productName,
                @NotNull @DecimalMin("0.01") BigDecimal price,
                @Min(1) int quantity) {}

        public record PlaceOrderRequest(
                @NotNull Long userId,
                @NotBlank String shippingName,
                @NotBlank String shippingAddress,
                @NotBlank String phone,
                @NotEmpty @Valid List<ItemRequest> items) {}

        public record UpdateStatusRequest(@NotNull OrderStatus status) {}

        // ---- outgoing ----
        public record ItemResponse(Long productId, String productName, BigDecimal price, int quantity, BigDecimal subtotal) {}

        public record OrderResponse(
                Long id,
                Long userId,
                OrderStatus status,
                BigDecimal total,
                Instant createdAt,
                String shippingName,
                String shippingAddress,
                String phone,
                List<ItemResponse> items) {}

        public static OrderResponse toResponse(Order o) {
            List<ItemResponse> items = o.getItems().stream()
                    .map(i -> new ItemResponse(i.getProductId(), i.getProductName(), i.getPrice(), i.getQuantity(), i.subtotal()))
                    .toList();
            return new OrderResponse(o.getId(), o.getUserId(), o.getStatus(), o.getTotal(), o.getCreatedAt(),
                    o.getShippingName(), o.getShippingAddress(), o.getPhone(), items);
        }
    }


