package com.example.orderservice.service;
import com.example.orderservice.Dto.OrderDto;
import com.example.orderservice.exception.OrderException;
import com.example.orderservice.models.Order;
import com.example.orderservice.models.OrderItem;
import com.example.orderservice.models.OrderStatus;
import com.example.orderservice.repository.OrderRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
public class OrderService {
    @Autowired
    private final OrderRepository orders;

    public OrderService(OrderRepository orders) {
        this.orders = orders;
    }
    private Order find(Long id) {
        return orders.findById(id)
                .orElseThrow(() -> new OrderException(HttpStatus.NOT_FOUND, "Order " + id + " not found"));
    }
    //Placing Order
    public OrderDto.OrderResponse placeOrder(OrderDto.PlaceOrderRequest request) {
        Order order = new Order();
        order.setUserId(request.userId());
        order.setShippingName(request.shippingName());
        order.setShippingAddress(request.shippingAddress());
        order.setPhone(request.phone());

        BigDecimal total = BigDecimal.ZERO;
        for (OrderDto.ItemRequest i : request.items()) {
            OrderItem item = new OrderItem(i.productId(), i.productName(), i.price(), i.quantity());
            order.addItem(item);
            total = total.add(item.subtotal());
        }
        order.setTotal(total);
        order.setStatus(OrderStatus.PLACED);

        return OrderDto.toResponse(orders.save(order));
    }
    //Finding order by id.
    @Transactional(readOnly = true)
    public OrderDto.OrderResponse getOrder(Long id) {
        return OrderDto.toResponse(find(id));
    }
    //retrieving order history
    @Transactional(readOnly = true)
    public List<OrderDto.OrderResponse> historyOf(Long userId) {
        return orders.findByUserIdOrderByCreatedAtDesc(userId)
                .stream().map(OrderDto::toResponse).toList();
    }
    //retrieving all orders
    @Transactional(readOnly = true)
    public List<OrderDto.OrderResponse> allOrders() {
        return orders.findAllByOrderByCreatedAtDesc()
                .stream().map(OrderDto::toResponse).toList();
    }
    //updating order status
    public OrderDto.OrderResponse updateStatus(Long id, OrderStatus next) {
        Order order = find(id);
        if (!order.getStatus().canMoveTo(next)) {
            throw new OrderException(HttpStatus.CONFLICT,
                    "Cannot change status from " + order.getStatus() + " to " + next);
        }
        order.setStatus(next);
        return OrderDto.toResponse(orders.save(order));
    }
    //Cancelling the order.
    public OrderDto.OrderResponse cancel(Long id) {
        return updateStatus(id, OrderStatus.CANCELLED);
    }

}
