package org.example.com.orderservice.kafka;

import org.example.com.orderservice.pojo.OrderRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

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
        String payload = "orderId=%s;productId=%s;quantity=%d"
                .formatted(orderRecord.getId(), orderRecord.getProductId(), orderRecord.getQuantity());
        kafkaTemplate.send(orderCreatedTopic, orderRecord.getId(), payload);
    }

    public void publishOrderCancelled(OrderRecord orderRecord) {
        String payload = "orderId=%s;productId=%s;quantity=%d"
                .formatted(orderRecord.getId(), orderRecord.getProductId(), orderRecord.getQuantity());
        kafkaTemplate.send(orderCancelledTopic, orderRecord.getId(), payload);
    }
}
