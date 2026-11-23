package com.example.orderservice.models;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id")
    private Order order;

    @Column(nullable = false)
    private Long productId;                    // product in the Product service, no foreign key

    @Column(nullable = false)
    private String productName;                // copied at order time

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;                  // unit price copied at order time

    @Column(nullable = false)
    private int quantity;

    protected OrderItem() {}                   // required by JPA

    public OrderItem(Long productId, String productName, BigDecimal price, int quantity) {
        this.productId = productId;
        this.productName = productName;
        this.price = price;
        this.quantity = quantity;
    }

    public BigDecimal subtotal() {             // not stored, calculated
        return price.multiply(BigDecimal.valueOf(quantity));
    }

    public Long getId() { return id; }
    public Order getOrder() { return order; }
    public void setOrder(Order order) { this.order = order; }
    public Long getProductId() { return productId; }
    public String getProductName() { return productName; }
    public BigDecimal getPrice() { return price; }
    public int getQuantity() { return quantity; }
}
