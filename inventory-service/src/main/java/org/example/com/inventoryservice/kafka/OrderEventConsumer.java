package org.example.com.inventoryservice.kafka;

import org.example.com.inventoryservice.service.InventoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class OrderEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderEventConsumer.class);

    private final InventoryService inventoryService;
    private final boolean testFailureEnabled;

    public OrderEventConsumer(
            InventoryService inventoryService,
            @Value("${app.kafka.enable-test-failure:false}") boolean testFailureEnabled
    ) {
        this.inventoryService = inventoryService;
        this.testFailureEnabled = testFailureEnabled;
    }

    @KafkaListener(topics = "${app.kafka.topic.order-created}", groupId = "${spring.kafka.consumer.group-id}")
    public void onOrderCreated(String message) {
        String eventId = valueOf(message, "eventId");
        String orderId = valueOf(message, "orderId");
        failWhenRequested(message, eventId, orderId);
        String productId = valueOf(message, "productId");
        Integer quantity = intValueOf(message, "quantity");
        if (orderId != null && productId != null && quantity != null) {
            inventoryService.reserve(eventId, orderId, productId, quantity);
        }
    }

    @KafkaListener(topics = "${app.kafka.topic.order-cancelled}", groupId = "${spring.kafka.consumer.group-id}")
    public void onOrderCancelled(String message) {
        String eventId = valueOf(message, "eventId");
        String orderId = valueOf(message, "orderId");
        String productId = valueOf(message, "productId");
        Integer quantity = intValueOf(message, "quantity");
        if (orderId != null && productId != null && quantity != null) {
            inventoryService.release(eventId, orderId, productId, quantity);
        }
    }

    private void failWhenRequested(String message, String eventId, String orderId) {
        if (testFailureEnabled && "true".equalsIgnoreCase(valueOf(message, "forceFailure"))) {
            log.warn("Forcing inventory consumer failure eventId={} orderId={}", eventId, orderId);
            throw new IllegalStateException("Forced inventory consumer failure for DLT verification");
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
