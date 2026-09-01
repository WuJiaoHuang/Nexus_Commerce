package org.example.com.inventoryservice.impl;

import org.example.com.inventoryservice.cache.InventoryCacheService;
import org.example.com.inventoryservice.dao.InventoryRepository;
import org.example.com.inventoryservice.dao.InventoryReservationRepository;
import org.example.com.inventoryservice.dao.ProcessedEventRepository;
import org.example.com.inventoryservice.kafka.InventoryEventProducer;
import org.example.com.inventoryservice.pojo.InventoryItem;
import org.example.com.inventoryservice.pojo.InventoryReservation;
import org.example.com.inventoryservice.pojo.ProcessedEvent;
import org.example.com.inventoryservice.service.InventoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;
    private final InventoryReservationRepository inventoryReservationRepository;
    private final ProcessedEventRepository processedEventRepository;
    private final InventoryEventProducer inventoryEventProducer;
    private final InventoryCacheService inventoryCacheService;

    public InventoryServiceImpl(InventoryRepository inventoryRepository,
                                InventoryReservationRepository inventoryReservationRepository,
                                ProcessedEventRepository processedEventRepository,
                                InventoryEventProducer inventoryEventProducer,
                                InventoryCacheService inventoryCacheService) {
        this.inventoryRepository = inventoryRepository;
        this.inventoryReservationRepository = inventoryReservationRepository;
        this.processedEventRepository = processedEventRepository;
        this.inventoryEventProducer = inventoryEventProducer;
        this.inventoryCacheService = inventoryCacheService;
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryItem getInventory(String productId) {
        return inventoryCacheService.getInventory(productId,
                () -> inventoryRepository.findById(productId).orElse(new InventoryItem(productId, 0)));
    }

    @Override
    @Transactional
    public InventoryItem upsertInventory(String productId, Integer quantity) {
        InventoryItem item = inventoryRepository.findById(productId)
                .orElse(new InventoryItem(productId, 0));
        item.setAvailableQuantity(quantity);
        InventoryItem saved = inventoryRepository.save(item);
        inventoryCacheService.cacheInventory(saved);
        return saved;
    }

    @Override
    @Transactional
    public void reserve(String eventId, String orderId, String productId, Integer quantity) {
        if (isProcessed(eventId)) {
            return;
        }

        if (inventoryReservationRepository.existsById(orderId)) {
            markProcessed(eventId, "ORDER_CREATED", orderId);
            inventoryEventProducer.publishReserved(orderId);
            return;
        }

        InventoryItem item = inventoryRepository.findById(productId)
                .orElse(new InventoryItem(productId, 0));

        if (quantity == null || quantity <= 0) {
            inventoryEventProducer.publishRejected(orderId, "INVALID_QUANTITY");
            markProcessed(eventId, "ORDER_CREATED", orderId);
            return;
        }

        if (item.getAvailableQuantity() >= quantity) {
            item.setAvailableQuantity(item.getAvailableQuantity() - quantity);
            inventoryCacheService.cacheInventory(inventoryRepository.save(item));
            inventoryReservationRepository.save(new InventoryReservation(orderId, productId, quantity));
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
        InventoryReservation reservation = inventoryReservationRepository.findById(orderId).orElse(null);
        if (reservation == null) {
            markProcessed(eventId, "ORDER_CANCELLED", orderId);
            return;
        }
        InventoryItem item = inventoryRepository.findById(reservation.getProductId())
                .orElse(new InventoryItem(reservation.getProductId(), 0));
        item.setAvailableQuantity(item.getAvailableQuantity() + reservation.getQuantity());
        inventoryCacheService.cacheInventory(inventoryRepository.save(item));
        inventoryReservationRepository.deleteById(orderId);
        inventoryEventProducer.publishReleased(orderId);
        markProcessed(eventId, "ORDER_CANCELLED", orderId);
    }

    private boolean isProcessed(String eventId) {
        return eventId != null && processedEventRepository.existsById(eventId);
    }

    private void markProcessed(String eventId, String eventType, String aggregateId) {
        if (eventId != null && !eventId.isBlank()) {
            processedEventRepository.save(new ProcessedEvent(eventId, eventType, aggregateId, LocalDateTime.now()));
        }
    }
}
