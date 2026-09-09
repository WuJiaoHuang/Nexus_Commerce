package org.example.com.orderservice.service.impl;

import org.example.com.common.exception.BusinessException;
import org.example.com.common.exception.ErrorCode;
import org.example.com.orderservice.kafka.OrderEventProducer;
import org.example.com.orderservice.mapper.OrderMapper;
import org.example.com.orderservice.pojo.OrderRecord;
import org.example.com.orderservice.service.OrderService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderMapper orderMapper;
    private final OrderEventProducer orderEventProducer;

    public OrderServiceImpl(OrderMapper orderMapper, OrderEventProducer orderEventProducer) {
        this.orderMapper = orderMapper;
        this.orderEventProducer = orderEventProducer;
    }

    @Override
    @Transactional
    public OrderRecord createOrder(OrderRecord orderRecord) {
        if (orderRecord.getQuantity() == null || orderRecord.getQuantity() <= 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "quantity must be greater than 0");
        }
        if (orderRecord.getId() == null || orderRecord.getId().isBlank()) {
            orderRecord.setId(UUID.randomUUID().toString());
        }
        orderRecord.setStatus("CREATED");
        orderRecord.setCreatedAt(LocalDateTime.now());

        orderMapper.insert(orderRecord);
        orderEventProducer.publishOrderCreated(orderRecord);
        return orderRecord;
    }

    @Override
    @Transactional(readOnly = true)
    public OrderRecord getOrder(String orderId) {
        OrderRecord orderRecord = orderMapper.selectById(orderId);
        if (orderRecord == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "订单不存在: " + orderId);
        }
        return orderRecord;
    }

    @Override
    @Transactional
    public OrderRecord cancelOrder(String orderId) {
        OrderRecord orderRecord = getOrder(orderId);
        if ("CANCELLED".equals(orderRecord.getStatus())) {
            return orderRecord;
        }
        orderRecord.setStatus("CANCELLING");
        orderMapper.updateById(orderRecord);
        orderEventProducer.publishOrderCancelled(orderRecord);
        return orderRecord;
    }

    @Override
    @Transactional
    public void markOrderReserved(String orderId) {
        OrderRecord orderRecord = getOrder(orderId);
        orderRecord.setStatus("RESERVED");
        orderMapper.updateById(orderRecord);
    }

    @Override
    @Transactional
    public void markOrderRejected(String orderId, String reason) {
        OrderRecord orderRecord = getOrder(orderId);
        orderRecord.setStatus("REJECTED:" + reason);
        orderMapper.updateById(orderRecord);
    }

    @Override
    @Transactional
    public void markOrderCancelled(String orderId) {
        OrderRecord orderRecord = getOrder(orderId);
        orderRecord.setStatus("CANCELLED");
        orderMapper.updateById(orderRecord);
    }
}
