package org.example.com.orderservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OrderPreviewResponse {
    private String productId;
    private Integer requestedQuantity;
    private Boolean productExists;
    private Integer availableQuantity;
    private Boolean reservable;
    private String message;
}
