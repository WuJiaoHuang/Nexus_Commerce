package org.example.com.orderservice.workflow;

import org.example.com.orderservice.dto.InventoryView;
import org.example.com.orderservice.dto.OrderPreviewRequest;
import org.example.com.orderservice.dto.OrderPreviewResponse;
import org.example.com.orderservice.dto.ProductView;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

@Service
public class OrderWorkflowServiceImpl implements OrderWorkflowService {

    private final RestTemplate restTemplate;
    private final AsyncTaskExecutor orderWorkflowExecutor;

    public OrderWorkflowServiceImpl(RestTemplate restTemplate,
                                    @Qualifier("orderWorkflowExecutor") AsyncTaskExecutor orderWorkflowExecutor) {
        this.restTemplate = restTemplate;
        this.orderWorkflowExecutor = orderWorkflowExecutor;
    }

    @Override
    public OrderPreviewResponse preview(OrderPreviewRequest request) {
        if (request == null || request.getProductId() == null || request.getProductId().isBlank()) {
            return new OrderPreviewResponse(null, null, false, 0, false, "productId is required");
        }
        if (request.getQuantity() == null || request.getQuantity() <= 0) {
            return new OrderPreviewResponse(request.getProductId(), request.getQuantity(), false, 0, false,
                    "quantity must be greater than 0");
        }

        CompletableFuture<Optional<ProductView>> productFuture = CompletableFuture.supplyAsync(
                () -> fetchProduct(request.getProductId()), orderWorkflowExecutor);

        CompletableFuture<InventoryView> inventoryFuture = CompletableFuture.supplyAsync(
                () -> fetchInventory(request.getProductId()), orderWorkflowExecutor);

        try {
            CompletableFuture.allOf(productFuture, inventoryFuture).join();
            Optional<ProductView> product = productFuture.join();
            InventoryView inventory = inventoryFuture.join();

            if (product.isEmpty()) {
                return new OrderPreviewResponse(request.getProductId(), request.getQuantity(), false,
                        0, false, "product not found");
            }

            int available = inventory.getAvailableQuantity() == null ? 0 : inventory.getAvailableQuantity();
            boolean reservable = available >= request.getQuantity();
            String message = reservable ? "ready to place order" : "insufficient inventory";

            return new OrderPreviewResponse(request.getProductId(), request.getQuantity(), true,
                    available, reservable, message);
        } catch (CompletionException ex) {
            return new OrderPreviewResponse(request.getProductId(), request.getQuantity(), false,
                    0, false, "downstream service error: " + ex.getCause().getMessage());
        }
    }

    private Optional<ProductView> fetchProduct(String productId) {
        try {
            ProductView product = restTemplate.getForObject(
                    "http://PRODUCT-SERVICE/product/{id}", ProductView.class, productId);
            return Optional.ofNullable(product);
        } catch (HttpClientErrorException ex) {
            HttpStatusCode status = ex.getStatusCode();
            if (status.value() == 404) {
                return Optional.empty();
            }
            throw ex;
        }
    }

    private InventoryView fetchInventory(String productId) {
        InventoryView inventory = restTemplate.getForObject(
                "http://INVENTORY-SERVICE/inventory/{productId}", InventoryView.class, productId);
        if (inventory == null) {
            InventoryView fallback = new InventoryView();
            fallback.setProductId(productId);
            fallback.setAvailableQuantity(0);
            return fallback;
        }
        return inventory;
    }
}
