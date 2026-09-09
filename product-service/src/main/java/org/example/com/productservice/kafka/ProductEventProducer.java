package org.example.com.productservice.kafka;

import org.example.com.productservice.pojo.Product;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class ProductEventProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final String productCreatedTopic;

    public ProductEventProducer(
            KafkaTemplate<String, String> kafkaTemplate,
            @Value("${app.kafka.topic.product-created}") String productCreatedTopic
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.productCreatedTopic = productCreatedTopic;
    }

    public void publishProductCreated(Product product) {
        String payload = "PRODUCT_CREATED:id=%s,name=%s,price=%s,stock=%s"
                .formatted(product.getId(), product.getName(), product.getPrice(), product.getStock());
        kafkaTemplate.send(productCreatedTopic, product.getId(), payload);
    }
}
