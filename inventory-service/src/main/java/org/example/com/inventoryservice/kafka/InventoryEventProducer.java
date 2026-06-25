package org.example.com.inventoryservice.kafka;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class InventoryEventProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final String inventoryReservedTopic;
    private final String inventoryRejectedTopic;

    public InventoryEventProducer(
            KafkaTemplate<String, String> kafkaTemplate,
            @Value("${app.kafka.topic.inventory-reserved}") String inventoryReservedTopic,
            @Value("${app.kafka.topic.inventory-rejected}") String inventoryRejectedTopic
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.inventoryReservedTopic = inventoryReservedTopic;
        this.inventoryRejectedTopic = inventoryRejectedTopic;
    }

    public void publishReserved(String orderId) {
        kafkaTemplate.send(inventoryReservedTopic, orderId, "orderId=%s".formatted(orderId));
    }

    public void publishRejected(String orderId, String reason) {
        kafkaTemplate.send(inventoryRejectedTopic, orderId, "orderId=%s;reason=%s".formatted(orderId, reason));
    }
}
