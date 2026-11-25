package com.example.orderservice.service;
import com.example.orderservice.Dto.ItemRequest;
import com.example.orderservice.Dto.OrderResponse;
import com.example.orderservice.Dto.PlaceOrderRequest;
import com.example.orderservice.exception.OrderException;
import com.example.orderservice.models.Order;
import com.example.orderservice.models.OrderItem;
import com.example.orderservice.models.OrderStatus;
import com.example.orderservice.repository.OrderRepository;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** All business rules of the Order service. Every method runs in one database transaction. */
@Service
@Transactional
public class OrderService {

    private final OrderRepository orders;

    public OrderService(OrderRepository orders) {
        this.orders = orders;
    }


    /** Creates the order and its items, calculates the total, and starts it as PLACED. */
    public OrderResponse placeOrder(PlaceOrderRequest request) {
        Order order = new Order();
        order.setUserId(request.userId());
        order.setShippingName(request.shippingName());
        order.setShippingAddress(request.shippingAddress());
        order.setPhone(request.phone());

        BigDecimal total = BigDecimal.ZERO;
        for (ItemRequest i : request.items()) {
            OrderItem item = new OrderItem(i.productId(), i.productName(), i.price(), i.quantity());
            order.addItem(item);
            total = total.add(item.subtotal());
        }
        order.setTotal(total);
        order.setStatus(OrderStatus.PLACED);

        return OrderResponse.from(orders.save(order));
    }


    @Transactional(readOnly = true)
    public OrderResponse getOrder(Long id) {
        return OrderResponse.from(find(id));
    }

    /** Order history of one customer, newest first. */
    @Transactional(readOnly = true)
    public List<OrderResponse> historyOfOrder(Long userId) {
        return orders.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(OrderResponse::from).toList();
    }

    /** Every order, newest first. */
    @Transactional(readOnly = true)
    public List<OrderResponse> allOrders() {
        return orders.findAllByOrderByCreatedAtDesc().stream()
                .map(OrderResponse::from).toList();
    }

    /** Moves an order along: PLACED, CONFIRMED, SHIPPED, DELIVERED. Anything else is rejected with 409. */
    public OrderResponse updateStatusOfOrder(Long id, OrderStatus next) {
        Order order = find(id);
        if (!order.getStatus().canMoveTo(next)) {
            throw new OrderException(HttpStatus.CONFLICT,
                    "Cannot change status from " + order.getStatus() + " to " + next);
        }
        order.setStatus(next);
        return OrderResponse.from(orders.save(order));
    }

    /** Cancel is allowed only while the order is PLACED or CONFIRMED. */
    public OrderResponse cancel(Long id) {
        return updateStatusOfOrder(id, OrderStatus.CANCELLED);
    }

    private Order find(Long id) {
        return orders.findById(id)
                .orElseThrow(() -> new OrderException(HttpStatus.NOT_FOUND, "Order " + id + " not found"));
    }
}
