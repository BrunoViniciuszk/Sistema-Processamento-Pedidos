package com.example.order_api.controller;

import com.example.order_api.dto.CreateOrderRequest;
import com.example.order_api.dto.OrderResponse;
import com.example.order_api.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService service;

    @PostMapping
    public ResponseEntity<OrderResponse> create(@RequestBody CreateOrderRequest request) {
        return ResponseEntity.status(201).body(service.create(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> get(@PathVariable UUID id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping("/{id}/events")
    public ResponseEntity<?> getEvents(@PathVariable UUID id) {
        return ResponseEntity.ok(service.getEvents(id));
    }
}