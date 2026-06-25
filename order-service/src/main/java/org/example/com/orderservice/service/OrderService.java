package org.example.com.orderservice.service;

import org.example.com.orderservice.pojo.OrderRecord;

public interface OrderService {
    OrderRecord createOrder(OrderRecord orderRecord);
    OrderRecord getOrder(String orderId);
    OrderRecord cancelOrder(String orderId);
    void markOrderReserved(String orderId);
    void markOrderRejected(String orderId, String reason);
}
