package org.example.com.inventoryservice.kafka;

import org.example.com.inventoryservice.service.InventoryService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class OrderEventConsumer {

    private final InventoryService inventoryService;

    public OrderEventConsumer(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @KafkaListener(topics = "${app.kafka.topic.order-created}", groupId = "${spring.kafka.consumer.group-id}")
    public void onOrderCreated(String message) {
        String orderId = valueOf(message, "orderId");
        String productId = valueOf(message, "productId");
        Integer quantity = intValueOf(message, "quantity");
        if (orderId != null && productId != null && quantity != null) {
            inventoryService.reserve(orderId, productId, quantity);
        }
    }

    @KafkaListener(topics = "${app.kafka.topic.order-cancelled}", groupId = "${spring.kafka.consumer.group-id}")
    public void onOrderCancelled(String message) {
        String orderId = valueOf(message, "orderId");
        String productId = valueOf(message, "productId");
        Integer quantity = intValueOf(message, "quantity");
        if (orderId != null && productId != null && quantity != null) {
            inventoryService.release(orderId, productId, quantity);
        }
    }

    private String valueOf(String message, String key) {
        String[] parts = message.split(";");
        for (String part : parts) {
            String[] pair = part.split("=", 2);
            if (pair.length == 2 && pair[0].equals(key)) {
                return pair[1];
            }
        }
        return null;
    }

    private Integer intValueOf(String message, String key) {
        String value = valueOf(message, key);
        if (value == null) {
            return null;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
