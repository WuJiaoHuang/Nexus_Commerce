package org.example.com.inventoryservice.controller;

import org.example.com.inventoryservice.pojo.InventoryItem;
import org.example.com.inventoryservice.service.InventoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping("/{productId}")
    public ResponseEntity<InventoryItem> getInventory(@PathVariable String productId) {
        return ResponseEntity.ok(inventoryService.getInventory(productId));
    }

    @PutMapping("/{productId}")
    public ResponseEntity<InventoryItem> upsertInventory(
            @PathVariable String productId,
            @RequestBody Map<String, Integer> body
    ) {
        Integer quantity = body.get("quantity");
        return ResponseEntity.ok(inventoryService.upsertInventory(productId, quantity == null ? 0 : quantity));
    }
}
