package org.example.com.orderservice.kafka;

import org.example.com.orderservice.pojo.OrderRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class OrderEventProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final String orderCreatedTopic;
    private final String orderCancelledTopic;

    public OrderEventProducer(
            KafkaTemplate<String, String> kafkaTemplate,
            @Value("${app.kafka.topic.order-created}") String orderCreatedTopic,
            @Value("${app.kafka.topic.order-cancelled}") String orderCancelledTopic
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.orderCreatedTopic = orderCreatedTopic;
        this.orderCancelledTopic = orderCancelledTopic;
    }

    public void publishOrderCreated(OrderRecord orderRecord) {
        String payload = payload("ORDER_CREATED", orderRecord);
        kafkaTemplate.send(orderCreatedTopic, orderRecord.getId(), payload);
    }

    public void publishOrderCancelled(OrderRecord orderRecord) {
        String payload = payload("ORDER_CANCELLED", orderRecord);
        kafkaTemplate.send(orderCancelledTopic, orderRecord.getId(), payload);
    }

    private String payload(String eventType, OrderRecord orderRecord) {
        return "eventId=%s;eventType=%s;orderId=%s;userId=%s;productId=%s;quantity=%d;createdAt=%s"
                .formatted(UUID.randomUUID(), eventType, orderRecord.getId(), orderRecord.getUserId(),
                        orderRecord.getProductId(), orderRecord.getQuantity(), LocalDateTime.now());
    }
}
