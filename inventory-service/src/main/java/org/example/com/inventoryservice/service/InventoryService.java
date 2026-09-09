package org.example.com.inventoryservice.service;

import org.example.com.inventoryservice.pojo.InventoryItem;

public interface InventoryService {
    InventoryItem getInventory(String productId);
    InventoryItem upsertInventory(String productId, Integer quantity);
    void reserve(String eventId, String orderId, String productId, Integer quantity);
    void release(String eventId, String orderId, String productId, Integer quantity);
}
