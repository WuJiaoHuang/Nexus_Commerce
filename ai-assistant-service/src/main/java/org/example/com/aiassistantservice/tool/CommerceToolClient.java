package org.example.com.aiassistantservice.tool;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class CommerceToolClient {

    private final RestTemplate restTemplate;

    public CommerceToolClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Retry(name = "orderService")
    @CircuitBreaker(name = "orderService", fallbackMethod = "queryOrderFallback")
    public ToolResult queryOrder(String orderId) {
        return get("order", "http://ORDER-SERVICE/orders/{id}", orderId);
    }

    @Retry(name = "productService")
    @CircuitBreaker(name = "productService", fallbackMethod = "queryProductFallback")
    public ToolResult queryProduct(String productId) {
        return get("product", "http://PRODUCT-SERVICE/product/{id}", productId);
    }

    @Retry(name = "inventoryService")
    @CircuitBreaker(name = "inventoryService", fallbackMethod = "queryInventoryFallback")
    public ToolResult queryInventory(String productId) {
        return get("inventory", "http://INVENTORY-SERVICE/inventory/{productId}", productId);
    }

    public ToolResult queryOrderFallback(String orderId, Throwable throwable) {
        return ToolResult.failure(
                "order",
                "Order service is temporarily unavailable"
        );
    }

    public ToolResult queryProductFallback(String productId, Throwable throwable) {
        return ToolResult.failure(
                "product",
                "Product service is temporarily unavailable"
        );
    }

    public ToolResult queryInventoryFallback(String productId, Throwable throwable) {
        return ToolResult.failure(
                "inventory",
                "Inventory service is temporarily unavailable"
        );
    }

    public ToolResult queryAfterSalesRule(String keyword) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("keyword", keyword);
        payload.put("rule", "Orders can be cancelled before inventory is reserved. Out-of-stock orders are rejected automatically.");
        payload.put("source", "local-policy-rule");
        return ToolResult.success("afterSalesRule", payload);
    }

    private ToolResult get(String toolName, String url, String id) {
        Map<?, ?> response = restTemplate.getForObject(url, Map.class, id);
        return ToolResult.success(toolName, response == null ? Map.of() : response);
    }
}
