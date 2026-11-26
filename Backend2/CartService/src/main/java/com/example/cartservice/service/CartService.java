package com.example.cartservice.service;
import com.example.cartservice.client.ProductLookup;
import com.example.cartservice.dto.Dtos;
import com.example.cartservice.dto.Dtos.AddItemRequest;
import com.example.cartservice.dto.Dtos.CartResponse;
import com.example.cartservice.models.Cart;
import com.example.cartservice.models.CartItem;
import com.example.cartservice.exception.CartException;
import com.example.cartservice.repository.CartRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.cartservice.dto.Dtos.ItemResponse;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
public class CartService {

    private static final int MAX_QUANTITY = 20;

    private final CartRepository carts;
    private final ProductLookup products;

    public CartService(CartRepository carts, ProductLookup products) {
        this.carts = carts;
        this.products = products;
    }
    // converts a Cart entity into the response sent to the client
    private CartResponse toResponse(Cart cart) {
        List<ItemResponse> items = cart.getItems().stream()
                .map(i -> new ItemResponse(i.getProductId(), i.getProductName(),
                        i.getPrice(), i.getQuantity(), i.subtotal()))
                .toList();

        int count = items.stream().mapToInt(ItemResponse::quantity).sum();        // number of units
        BigDecimal total = items.stream().map(ItemResponse::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);                        // sum of subtotals

        return new CartResponse(items, count, total);
    }

    // 1. Show the cart
    @Transactional(readOnly = true)
    public CartResponse getCart(Long userId) {
        return carts.findByUserId(userId)
                .map(Dtos::toResponse)
                .orElseGet(Dtos::empty);                 // no cart yet = empty cart
    }

    // 2. Add a product
    public CartResponse addItem(Long userId, AddItemRequest request) {
        ProductLookup.ProductInfo product = products.find(request.productId());          // 404 if unknown
        Cart cart = carts.findByUserId(userId).orElseGet(() -> new Cart(userId));

        CartItem existing = cart.findItem(product.id());
        if (existing == null) {
            checkLimit(request.quantity());
            cart.addItem(new CartItem(product.id(), product.name(), product.price(), request.quantity()));
        } else {
            int newQuantity = existing.getQuantity() + request.quantity();
            checkLimit(newQuantity);
            existing.setQuantity(newQuantity);                              // merge into one line
            existing.setProductName(product.name());
            existing.setPrice(product.price());
        }
        return toResponse(carts.save(cart));
    }

    // 3. Set an exact quantity
    public CartResponse updateQuantity(Long userId, Long productId, int quantity) {
        checkLimit(quantity);
        Cart cart = cartOf(userId);
        itemOf(cart, productId).setQuantity(quantity);
        return toResponse(carts.save(cart));
    }

    // 4. Remove one product
    public CartResponse removeItem(Long userId, Long productId) {
        Cart cart = cartOf(userId);
        cart.removeItem(itemOf(cart, productId));
        return toResponse(carts.save(cart));
    }

    // 5. Empty the cart (safe to call twice)
    public void clear(Long userId) {
        carts.findByUserId(userId).ifPresent(carts::delete);
    }

    // ---- helpers ----

    private Cart cartOf(Long userId) {
        return carts.findByUserId(userId)
                .orElseThrow(() -> new CartException(HttpStatus.NOT_FOUND, "Your cart is empty"));
    }

    private CartItem itemOf(Cart cart, Long productId) {
        CartItem item = cart.findItem(productId);
        if (item == null) {
            throw new CartException(HttpStatus.NOT_FOUND, "Product " + productId + " is not in your cart");
        }
        return item;
    }

    private void checkLimit(int quantity) {
        if (quantity > MAX_QUANTITY) {
            throw new CartException(HttpStatus.BAD_REQUEST,
                    "You can add at most " + MAX_QUANTITY + " of one product");
        }
    }
}
