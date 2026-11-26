package com.example.cartservice.models;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "carts")
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;                 // user in the User service, no foreign key

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItem> items = new ArrayList<>();

    protected Cart() {}                  // required by JPA

    public Cart(Long userId) {
        this.userId = userId;
    }

    // keeps both sides of the relationship in sync
    public void addItem(CartItem item) {
        item.setCart(this);
        items.add(item);
    }

    public void removeItem(CartItem item) {
        items.remove(item);              // orphanRemoval deletes the row
    }

    // the line for a product, or null if it is not in the cart
    public CartItem findItem(Long productId) {
        return items.stream()
                .filter(i -> i.getProductId().equals(productId))
                .findFirst()
                .orElse(null);
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public List<CartItem> getItems() { return items; }
}
