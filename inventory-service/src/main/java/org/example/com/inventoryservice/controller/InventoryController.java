package org.example.com.inventoryservice.controller;

import org.example.com.common.result.Result;
import org.example.com.inventoryservice.dto.InventoryUpsertRequest;
import org.example.com.inventoryservice.pojo.InventoryItem;
import org.example.com.inventoryservice.service.InventoryService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping("/{productId}")
    public Result<InventoryItem> getInventory(@PathVariable String productId) {
        return Result.success(inventoryService.getInventory(productId));
    }

    @PutMapping("/{productId}")
    public Result<InventoryItem> upsertInventory(
            @PathVariable String productId,
            @RequestBody InventoryUpsertRequest request
    ) {
        Integer quantity = request == null ? null : request.getQuantity();
        return Result.success(inventoryService.upsertInventory(productId, quantity == null ? 0 : quantity));
    }
}
