package org.example.com.orderservice.workflow;

import org.example.com.orderservice.dto.OrderPreviewRequest;
import org.example.com.orderservice.dto.OrderPreviewResponse;

public interface OrderWorkflowService {
    OrderPreviewResponse preview(OrderPreviewRequest request);
}
