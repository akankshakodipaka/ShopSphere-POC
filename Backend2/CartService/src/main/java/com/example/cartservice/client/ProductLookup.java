package com.example.cartservice.client;
import com.example.cartservice.exception.CartException;
import java.math.BigDecimal;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class ProductLookup {

    public record ProductInfo(Long id, String name, BigDecimal price) {}

    public ProductInfo find(Long id) {
        return switch (id.intValue()) {
            case 1 -> new ProductInfo(1L, "Headphones", new BigDecimal("2499.00"));
            case 2 -> new ProductInfo(2L, "T-Shirt", new BigDecimal("599.00"));
            case 3 -> new ProductInfo(3L, "Water Bottle", new BigDecimal("799.00"));
            default -> throw new CartException(HttpStatus.NOT_FOUND, "Product " + id + " not found");
        };
    }
}
