package org.example.com.orderservice;

import org.example.com.orderservice.dto.OrderPreviewRequest;
import org.example.com.orderservice.dto.OrderPreviewResponse;
import org.example.com.orderservice.workflow.OrderWorkflowServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;

class OrderWorkflowServiceImplTests {

    @Test
    void previewFallbackReturnsBusinessDegradedResponse() {
        OrderWorkflowServiceImpl service = new OrderWorkflowServiceImpl(
                mock(RestTemplate.class), new SimpleAsyncTaskExecutor());
        OrderPreviewRequest request = new OrderPreviewRequest();
        request.setProductId("product-1");
        request.setQuantity(2);

        OrderPreviewResponse response = service.previewFallback(request, new RuntimeException("downstream timeout"));

        assertEquals("product-1", response.getProductId());
        assertEquals(2, response.getRequestedQuantity());
        assertFalse(response.getProductExists());
        assertFalse(response.getReservable());
        assertEquals("preview temporarily degraded: downstream timeout", response.getMessage());
    }
}
