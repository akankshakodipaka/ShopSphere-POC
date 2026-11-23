package com.example.orderservice.controller;
import com.example.orderservice.Dto.OrderDto;
import com.example.orderservice.service.OrderService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Orders", description = "Place orders, view history, change status, cancel")
@RestController
@RequestMapping("/orders")
public class OrderController {
    private final OrderService service;

    public OrderController(OrderService service) {
        this.service = service;
    }

    // POST /orders : place an order
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderDto.OrderResponse place(@Valid @RequestBody OrderDto.PlaceOrderRequest request) {
        return service.placeOrder(request);
    }
    @GetMapping("/{id}")
    public OrderDto.OrderResponse one(@PathVariable Long id) {
        return service.getOrder(id);
    }
    // GET /orders          → every order
    // GET /orders?userId=7 → only that customer's orders
    @GetMapping
    public List<OrderDto.OrderResponse> list(@RequestParam(required = false) Long userId) {
        return userId == null ? service.allOrders() : service.historyOf(userId);
    }
    // PUT /orders/5/status  body: {"status":"CONFIRMED"}
    @PutMapping("/{id}/status")
    public OrderDto.OrderResponse updateStatus(@PathVariable Long id,
                                               @Valid @RequestBody OrderDto.UpdateStatusRequest request) {
        return service.updateStatus(id, request.status());
    }
    // POST /orders/5/cancel : only from PLACED or CONFIRMED
    @PostMapping("/{id}/cancel")
    public OrderDto.OrderResponse cancel(@PathVariable Long id) {
        return service.cancel(id);
    }

}
