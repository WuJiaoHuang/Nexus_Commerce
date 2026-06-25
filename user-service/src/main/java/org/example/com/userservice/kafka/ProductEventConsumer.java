package org.example.com.userservice.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class ProductEventConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProductEventConsumer.class);

    @KafkaListener(topics = "${app.kafka.topic.product-created}", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeProductCreated(String message) {
        LOGGER.info("Received product event: {}", message);
    }
}
