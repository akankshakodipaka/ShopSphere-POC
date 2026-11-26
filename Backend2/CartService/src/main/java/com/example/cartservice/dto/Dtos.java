package com.example.cartservice.dto;
import com.example.cartservice.models.Cart;
import com.example.cartservice.models.CartItem;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

public final class Dtos {

    private Dtos() {}

    // incoming
    public record AddItemRequest(@NotNull Long productId, @Min(1) int quantity) {}
    public record UpdateQuantityRequest(@Min(1) int quantity) {}

    // outgoing
    public record ItemResponse(Long productId, String productName, BigDecimal price,
                               int quantity, BigDecimal subtotal) {}
    public record CartResponse(List<ItemResponse> items, int itemCount, BigDecimal total) {}

    public static CartResponse empty() {
        return new CartResponse(List.of(), 0, BigDecimal.ZERO);
    }

    public static CartResponse toResponse(Cart cart) {
        List<ItemResponse> items = cart.getItems().stream().map(Dtos::toItem).toList();
        int count = items.stream().mapToInt(ItemResponse::quantity).sum();
        BigDecimal total = items.stream().map(ItemResponse::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new CartResponse(items, count, total);
    }

    private static ItemResponse toItem(CartItem i) {
        return new ItemResponse(i.getProductId(), i.getProductName(), i.getPrice(),
                i.getQuantity(), i.subtotal());
    }
}
