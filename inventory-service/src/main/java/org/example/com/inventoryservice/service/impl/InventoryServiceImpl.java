package org.example.com.inventoryservice.service.impl;

import org.example.com.inventoryservice.cache.InventoryCacheService;
import org.example.com.inventoryservice.kafka.InventoryEventProducer;
import org.example.com.inventoryservice.mapper.InventoryMapper;
import org.example.com.inventoryservice.mapper.InventoryReservationMapper;
import org.example.com.inventoryservice.mapper.ProcessedEventMapper;
import org.example.com.inventoryservice.pojo.InventoryItem;
import org.example.com.inventoryservice.pojo.InventoryReservation;
import org.example.com.inventoryservice.pojo.ProcessedEvent;
import org.example.com.inventoryservice.service.InventoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class InventoryServiceImpl implements InventoryService {

    private final InventoryMapper inventoryMapper;
    private final InventoryReservationMapper inventoryReservationMapper;
    private final ProcessedEventMapper processedEventMapper;
    private final InventoryEventProducer inventoryEventProducer;
    private final InventoryCacheService inventoryCacheService;

    public InventoryServiceImpl(InventoryMapper inventoryMapper,
                                InventoryReservationMapper inventoryReservationMapper,
                                ProcessedEventMapper processedEventMapper,
                                InventoryEventProducer inventoryEventProducer,
                                InventoryCacheService inventoryCacheService) {
        this.inventoryMapper = inventoryMapper;
        this.inventoryReservationMapper = inventoryReservationMapper;
        this.processedEventMapper = processedEventMapper;
        this.inventoryEventProducer = inventoryEventProducer;
        this.inventoryCacheService = inventoryCacheService;
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryItem getInventory(String productId) {
        return inventoryCacheService.getInventory(productId,
                () -> {
                    InventoryItem item = inventoryMapper.selectById(productId);
                    return item == null ? new InventoryItem(productId, 0) : item;
                });
    }

    @Override
    @Transactional
    public InventoryItem upsertInventory(String productId, Integer quantity) {
        InventoryItem item = inventoryMapper.selectById(productId);
        boolean exists = item != null;
        if (item == null) {
            item = new InventoryItem(productId, 0);
        }
        item.setAvailableQuantity(quantity);
        if (exists) {
            inventoryMapper.updateById(item);
        } else {
            inventoryMapper.insert(item);
        }
        inventoryCacheService.cacheInventory(item);
        return item;
    }

    @Override
    @Transactional
    public void reserve(String eventId, String orderId, String productId, Integer quantity) {
        if (isProcessed(eventId)) {
            return;
        }

        if (inventoryReservationMapper.selectById(orderId) != null) {
            markProcessed(eventId, "ORDER_CREATED", orderId);
            inventoryEventProducer.publishReserved(orderId);
            return;
        }

        InventoryItem item = inventoryMapper.selectById(productId);
        if (item == null) {
            item = new InventoryItem(productId, 0);
        }

        if (quantity == null || quantity <= 0) {
            inventoryEventProducer.publishRejected(orderId, "INVALID_QUANTITY");
            markProcessed(eventId, "ORDER_CREATED", orderId);
            return;
        }

        if (item.getAvailableQuantity() >= quantity) {
            item.setAvailableQuantity(item.getAvailableQuantity() - quantity);
            inventoryMapper.updateById(item);
            inventoryCacheService.cacheInventory(item);
            inventoryReservationMapper.insert(new InventoryReservation(orderId, productId, quantity));
            inventoryEventProducer.publishReserved(orderId);
            markProcessed(eventId, "ORDER_CREATED", orderId);
            return;
        }

        inventoryEventProducer.publishRejected(orderId, "OUT_OF_STOCK");
        markProcessed(eventId, "ORDER_CREATED", orderId);
    }

    @Override
    @Transactional
    public void release(String eventId, String orderId, String productId, Integer quantity) {
        if (isProcessed(eventId)) {
            return;
        }
        InventoryReservation reservation = inventoryReservationMapper.selectById(orderId);
        if (reservation == null) {
            markProcessed(eventId, "ORDER_CANCELLED", orderId);
            return;
        }
        InventoryItem item = inventoryMapper.selectById(reservation.getProductId());
        if (item == null) {
            item = new InventoryItem(reservation.getProductId(), 0);
            inventoryMapper.insert(item);
        }
        item.setAvailableQuantity(item.getAvailableQuantity() + reservation.getQuantity());
        inventoryMapper.updateById(item);
        inventoryCacheService.cacheInventory(item);
        inventoryReservationMapper.deleteById(orderId);
        inventoryEventProducer.publishReleased(orderId);
        markProcessed(eventId, "ORDER_CANCELLED", orderId);
    }

    private boolean isProcessed(String eventId) {
        return eventId != null && processedEventMapper.selectById(eventId) != null;
    }

    private void markProcessed(String eventId, String eventType, String aggregateId) {
        if (eventId != null && !eventId.isBlank()) {
            processedEventMapper.insert(new ProcessedEvent(eventId, eventType, aggregateId, LocalDateTime.now()));
        }
    }
}
