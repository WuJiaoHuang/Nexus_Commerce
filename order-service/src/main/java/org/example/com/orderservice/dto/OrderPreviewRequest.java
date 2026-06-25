package org.example.com.orderservice.dto;

import lombok.Data;

@Data
public class OrderPreviewRequest {
    private String productId;
    private Integer quantity;
}
