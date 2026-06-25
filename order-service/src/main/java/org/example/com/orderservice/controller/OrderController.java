package org.example.com.orderservice.controller;

import org.example.com.orderservice.dto.OrderPreviewRequest;
import org.example.com.orderservice.dto.OrderPreviewResponse;
import org.example.com.orderservice.pojo.OrderRecord;
import org.example.com.orderservice.service.OrderService;
import org.example.com.orderservice.workflow.OrderWorkflowService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;
    private final OrderWorkflowService orderWorkflowService;

    public OrderController(OrderService orderService, OrderWorkflowService orderWorkflowService) {
        this.orderService = orderService;
        this.orderWorkflowService = orderWorkflowService;
    }

    @PostMapping
    public ResponseEntity<OrderRecord> createOrder(@RequestBody OrderRecord orderRecord) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.createOrder(orderRecord));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderRecord> getOrder(@PathVariable String id) {
        return ResponseEntity.ok(orderService.getOrder(id));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<OrderRecord> cancelOrder(@PathVariable String id) {
        return ResponseEntity.ok(orderService.cancelOrder(id));
    }

    @PostMapping("/preview")
    public ResponseEntity<OrderPreviewResponse> preview(@RequestBody OrderPreviewRequest request) {
        return ResponseEntity.ok(orderWorkflowService.preview(request));
    }
}
