package org.example.com.orderservice.dto;

import lombok.Data;

@Data
public class InventoryView {
    private String productId;
    private Integer availableQuantity;
}
