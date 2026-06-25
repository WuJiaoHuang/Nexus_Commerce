package org.example.com.inventoryservice.impl;

import org.example.com.inventoryservice.dao.InventoryRepository;
import org.example.com.inventoryservice.dao.InventoryReservationRepository;
import org.example.com.inventoryservice.kafka.InventoryEventProducer;
import org.example.com.inventoryservice.pojo.InventoryItem;
import org.example.com.inventoryservice.pojo.InventoryReservation;
import org.example.com.inventoryservice.service.InventoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;
    private final InventoryReservationRepository inventoryReservationRepository;
    private final InventoryEventProducer inventoryEventProducer;

    public InventoryServiceImpl(InventoryRepository inventoryRepository,
                                InventoryReservationRepository inventoryReservationRepository,
                                InventoryEventProducer inventoryEventProducer) {
        this.inventoryRepository = inventoryRepository;
        this.inventoryReservationRepository = inventoryReservationRepository;
        this.inventoryEventProducer = inventoryEventProducer;
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryItem getInventory(String productId) {
        return inventoryRepository.findById(productId)
                .orElse(new InventoryItem(productId, 0));
    }

    @Override
    @Transactional
    public InventoryItem upsertInventory(String productId, Integer quantity) {
        InventoryItem item = inventoryRepository.findById(productId)
                .orElse(new InventoryItem(productId, 0));
        item.setAvailableQuantity(quantity);
        return inventoryRepository.save(item);
    }

    @Override
    @Transactional
    public void reserve(String orderId, String productId, Integer quantity) {
        InventoryItem item = inventoryRepository.findById(productId)
                .orElse(new InventoryItem(productId, 0));

        if (quantity == null || quantity <= 0) {
            inventoryEventProducer.publishRejected(orderId, "INVALID_QUANTITY");
            return;
        }

        if (item.getAvailableQuantity() >= quantity) {
            item.setAvailableQuantity(item.getAvailableQuantity() - quantity);
            inventoryRepository.save(item);
            inventoryReservationRepository.save(new InventoryReservation(orderId, productId, quantity));
            inventoryEventProducer.publishReserved(orderId);
            return;
        }

        inventoryEventProducer.publishRejected(orderId, "OUT_OF_STOCK");
    }

    @Override
    @Transactional
    public void release(String orderId, String productId, Integer quantity) {
        InventoryReservation reservation = inventoryReservationRepository.findById(orderId).orElse(null);
        if (reservation == null) {
            return;
        }
        InventoryItem item = inventoryRepository.findById(reservation.getProductId())
                .orElse(new InventoryItem(reservation.getProductId(), 0));
        item.setAvailableQuantity(item.getAvailableQuantity() + reservation.getQuantity());
        inventoryRepository.save(item);
        inventoryReservationRepository.deleteById(orderId);
    }
}
