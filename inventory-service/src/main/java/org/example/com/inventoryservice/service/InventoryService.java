package org.example.com.inventoryservice.service;

import org.example.com.inventoryservice.pojo.InventoryItem;

public interface InventoryService {
    InventoryItem getInventory(String productId);
    InventoryItem upsertInventory(String productId, Integer quantity);
    void reserve(String orderId, String productId, Integer quantity);
    void release(String orderId, String productId, Integer quantity);
}
