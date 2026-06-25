package org.example.com.orderservice.kafka;

import org.example.com.orderservice.service.OrderService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class InventoryResultConsumer {

    private final OrderService orderService;
    private final String inventoryRejectedTopic;

    public InventoryResultConsumer(OrderService orderService,
                                   @Value("${app.kafka.topic.inventory-rejected}") String inventoryRejectedTopic) {
        this.orderService = orderService;
        this.inventoryRejectedTopic = inventoryRejectedTopic;
    }

    @KafkaListener(topics = "${app.kafka.topic.inventory-reserved}", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeInventoryReserved(String message) {
        String orderId = valueOf(message, "orderId");
        if (orderId != null) {
            orderService.markOrderReserved(orderId);
        }
    }

    @KafkaListener(topics = "${app.kafka.topic.inventory-rejected}", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeInventoryRejected(String message) {
        String orderId = valueOf(message, "orderId");
        String reason = valueOf(message, "reason");
        if (orderId != null) {
            orderService.markOrderRejected(orderId, reason == null ? "UNKNOWN" : reason);
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
}
