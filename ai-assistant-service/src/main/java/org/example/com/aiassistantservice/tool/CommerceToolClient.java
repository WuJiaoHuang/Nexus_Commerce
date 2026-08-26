package org.example.com.aiassistantservice.tool;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class CommerceToolClient {

    private final RestTemplate restTemplate;

    public CommerceToolClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public ToolResult queryOrder(String orderId) {
        return get("order", "http://ORDER-SERVICE/orders/{id}", orderId);
    }

    public ToolResult queryProduct(String productId) {
        return get("product", "http://PRODUCT-SERVICE/product/{id}", productId);
    }

    public ToolResult queryInventory(String productId) {
        return get("inventory", "http://INVENTORY-SERVICE/inventory/{productId}", productId);
    }

    public ToolResult queryAfterSalesRule(String keyword) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("keyword", keyword);
        payload.put("rule", "Orders can be cancelled before inventory is reserved. Out-of-stock orders are rejected automatically.");
        payload.put("source", "local-policy-rule");
        return ToolResult.success("afterSalesRule", payload);
    }

    private ToolResult get(String toolName, String url, String id) {
        try {
            Map<?, ?> response = restTemplate.getForObject(url, Map.class, id);
            return ToolResult.success(toolName, response == null ? Map.of() : response);
        } catch (RestClientException ex) {
            return ToolResult.failure(toolName, ex.getMessage());
        }
    }
}
