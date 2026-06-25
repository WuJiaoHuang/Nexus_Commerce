package org.example.com.orderservice.impl;

import org.example.com.orderservice.dao.OrderRepository;
import org.example.com.orderservice.kafka.OrderEventProducer;
import org.example.com.orderservice.pojo.OrderRecord;
import org.example.com.orderservice.service.OrderService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderEventProducer orderEventProducer;

    public OrderServiceImpl(OrderRepository orderRepository, OrderEventProducer orderEventProducer) {
        this.orderRepository = orderRepository;
        this.orderEventProducer = orderEventProducer;
    }

    @Override
    @Transactional
    public OrderRecord createOrder(OrderRecord orderRecord) {
        if (orderRecord.getQuantity() == null || orderRecord.getQuantity() <= 0) {
            throw new IllegalArgumentException("quantity must be greater than 0");
        }
        if (orderRecord.getId() == null || orderRecord.getId().isBlank()) {
            orderRecord.setId(UUID.randomUUID().toString());
        }
        orderRecord.setStatus("CREATED");
        orderRecord.setCreatedAt(LocalDateTime.now());

        OrderRecord savedOrder = orderRepository.save(orderRecord);
        orderEventProducer.publishOrderCreated(savedOrder);
        return savedOrder;
    }

    @Override
    @Transactional(readOnly = true)
    public OrderRecord getOrder(String orderId) {
        return orderRepository.findById(orderId).orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));
    }

    @Override
    @Transactional
    public OrderRecord cancelOrder(String orderId) {
        OrderRecord orderRecord = getOrder(orderId);
        orderRecord.setStatus("CANCELLED");
        OrderRecord savedOrder = orderRepository.save(orderRecord);
        orderEventProducer.publishOrderCancelled(savedOrder);
        return savedOrder;
    }

    @Override
    @Transactional
    public void markOrderReserved(String orderId) {
        OrderRecord orderRecord = getOrder(orderId);
        orderRecord.setStatus("RESERVED");
        orderRepository.save(orderRecord);
    }

    @Override
    @Transactional
    public void markOrderRejected(String orderId, String reason) {
        OrderRecord orderRecord = getOrder(orderId);
        orderRecord.setStatus("REJECTED:" + reason);
        orderRepository.save(orderRecord);
    }
}
