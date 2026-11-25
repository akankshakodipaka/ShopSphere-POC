package com.example.orderservice.controller;
import com.example.orderservice.Dto.OrderResponse;
import com.example.orderservice.Dto.PlaceOrderRequest;
import com.example.orderservice.Dto.UpdateStatusRequest;
import com.example.orderservice.service.OrderService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Orders", description = "Place orders, view history, change status, cancel")
@RestController
@RequestMapping("/v1")
public class OrderController {
    private final OrderService service;

    public OrderController(OrderService service) {
        this.service = service;
    }

    // POST /orders : place an order
    @PostMapping("/createOrder")
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse placeOrder(@Valid @RequestBody PlaceOrderRequest request) {
        return service.placeOrder(request);
    }
    @GetMapping("getOrderById/{id}")
    public OrderResponse getOrder(@PathVariable Long id) {
        return service.getOrder(id);
    }
    // GET /orders          → every order
    // GET /orders?userId=7 → only that customer's orders
    @GetMapping("/getOrderDetail")
    public List<OrderResponse> allOrders(@RequestParam(required = false) Long userId) {
        return userId == null ? service.allOrders() : service.historyOfOrder(userId);
    }
    // PUT /orders/5/status  body: {"status":"CONFIRMED"}
    @PutMapping("/{id}/status")
    public OrderResponse updateStatusOfOrder(@PathVariable Long id,
                                               @Valid @RequestBody UpdateStatusRequest request) {
        return service.updateStatusOfOrder(id, request.status());
    }
    // POST /orders/5/cancel : only from PLACED or CONFIRMED
    @PostMapping("/{id}/cancel")
    public OrderResponse cancel(@PathVariable Long id) {
        return service.cancel(id);
    }

}
