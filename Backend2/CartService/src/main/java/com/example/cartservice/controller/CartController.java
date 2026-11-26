package com.example.cartservice.controller;
import com.example.cartservice.dto.Dtos.AddItemRequest;
import com.example.cartservice.dto.Dtos.CartResponse;
import com.example.cartservice.dto.Dtos.UpdateQuantityRequest;
import com.example.cartservice.service.CartService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1")
public class CartController {

    private final CartService service;

    public CartController(CartService service) {
        this.service = service;
    }

    // GET /cart : items, item count and total
    @GetMapping("/getCartDetails")
    public CartResponse getCart(@RequestHeader("X-User-Id") Long userId) {
        return service.getCart(userId);
    }

    // POST /cart/items   body: {"productId":1,"quantity":2}
    @PostMapping("/addItemsToCart")
    public CartResponse addItem(@RequestHeader("X-User-Id") Long userId,
                            @Valid @RequestBody AddItemRequest request) {
        return service.addItem(userId, request);
    }

    // PUT /cart/items/1   body: {"quantity":3}
    @PutMapping("/updateQuantityOfItems/{productId}")
    public CartResponse updateQuantity(@RequestHeader("X-User-Id") Long userId,
                               @PathVariable Long productId,
                               @Valid @RequestBody UpdateQuantityRequest request) {
        return service.updateQuantity(userId, productId, request.quantity());
    }

    // DELETE /cart/items/1 : remove one product
    @DeleteMapping("/removeItems/{productId}")
    public CartResponse removeItem(@RequestHeader("X-User-Id") Long userId,
                               @PathVariable Long productId) {
        return service.removeItem(userId, productId);
    }

    // DELETE /cart : empty the cart
    @DeleteMapping("clearCart")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void clear(@RequestHeader("X-User-Id") Long userId) {
        service.clear(userId);
    }
}
