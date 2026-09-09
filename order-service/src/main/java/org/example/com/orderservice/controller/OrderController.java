package org.example.com.orderservice.controller;

import org.example.com.common.result.Result;
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
    public ResponseEntity<Result<OrderRecord>> createOrder(@RequestBody OrderRecord orderRecord) {
        OrderRecord createdOrder = orderService.createOrder(orderRecord);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Result.success(HttpStatus.CREATED.value(), "created", createdOrder));
    }

    @GetMapping("/{id}")
    public Result<OrderRecord> getOrder(@PathVariable String id) {
        return Result.success(orderService.getOrder(id));
    }

    @PostMapping("/{id}/cancel")
    public Result<OrderRecord> cancelOrder(@PathVariable String id) {
        return Result.success(orderService.cancelOrder(id));
    }

    @PostMapping("/preview")
    public Result<OrderPreviewResponse> preview(@RequestBody OrderPreviewRequest request) {
        return Result.success(orderWorkflowService.preview(request));
    }
}
