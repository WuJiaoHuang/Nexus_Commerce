package org.example.com.inventoryservice;

import org.example.com.inventoryservice.cache.InventoryCacheService;
import org.example.com.inventoryservice.dao.InventoryRepository;
import org.example.com.inventoryservice.dao.InventoryReservationRepository;
import org.example.com.inventoryservice.dao.ProcessedEventRepository;
import org.example.com.inventoryservice.impl.InventoryServiceImpl;
import org.example.com.inventoryservice.kafka.InventoryEventProducer;
import org.example.com.inventoryservice.pojo.InventoryItem;
import org.example.com.inventoryservice.pojo.InventoryReservation;
import org.example.com.inventoryservice.pojo.ProcessedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceImplTests {

    @Mock
    InventoryRepository inventoryRepository;

    @Mock
    InventoryReservationRepository inventoryReservationRepository;

    @Mock
    ProcessedEventRepository processedEventRepository;

    @Mock
    InventoryEventProducer inventoryEventProducer;

    @Mock
    InventoryCacheService inventoryCacheService;

    InventoryServiceImpl inventoryService;

    @BeforeEach
    void setUp() {
        inventoryService = new InventoryServiceImpl(
                inventoryRepository,
                inventoryReservationRepository,
                processedEventRepository,
                inventoryEventProducer,
                inventoryCacheService);
    }

    @Test
    void reserveDecrementsInventoryAndPublishesReserved() {
        InventoryItem item = new InventoryItem("p-1", 5);
        when(processedEventRepository.existsById("event-1")).thenReturn(false);
        when(inventoryReservationRepository.existsById("order-1")).thenReturn(false);
        when(inventoryRepository.findById("p-1")).thenReturn(Optional.of(item));
        when(inventoryRepository.save(item)).thenReturn(item);

        inventoryService.reserve("event-1", "order-1", "p-1", 2);

        assertEquals(3, item.getAvailableQuantity());
        verify(inventoryReservationRepository).save(new InventoryReservation("order-1", "p-1", 2));
        verify(inventoryEventProducer).publishReserved("order-1");
        verify(processedEventRepository).save(any(ProcessedEvent.class));
    }

    @Test
    void reserveRejectsWhenInventoryIsInsufficient() {
        InventoryItem item = new InventoryItem("p-1", 1);
        when(processedEventRepository.existsById("event-1")).thenReturn(false);
        when(inventoryReservationRepository.existsById("order-1")).thenReturn(false);
        when(inventoryRepository.findById("p-1")).thenReturn(Optional.of(item));

        inventoryService.reserve("event-1", "order-1", "p-1", 2);

        assertEquals(1, item.getAvailableQuantity());
        verify(inventoryEventProducer).publishRejected("order-1", "OUT_OF_STOCK");
        verify(inventoryRepository, never()).save(any());
        verify(processedEventRepository).save(any(ProcessedEvent.class));
    }

    @Test
    void duplicateReserveEventDoesNotDecrementAgain() {
        when(processedEventRepository.existsById("event-1")).thenReturn(true);

        inventoryService.reserve("event-1", "order-1", "p-1", 2);

        verifyNoInteractions(inventoryRepository, inventoryReservationRepository, inventoryEventProducer, inventoryCacheService);
    }

    @Test
    void releaseRestoresInventoryAndPublishesReleased() {
        InventoryReservation reservation = new InventoryReservation("order-1", "p-1", 2);
        InventoryItem item = new InventoryItem("p-1", 3);
        when(processedEventRepository.existsById("cancel-1")).thenReturn(false);
        when(inventoryReservationRepository.findById("order-1")).thenReturn(Optional.of(reservation));
        when(inventoryRepository.findById("p-1")).thenReturn(Optional.of(item));
        when(inventoryRepository.save(item)).thenReturn(item);

        inventoryService.release("cancel-1", "order-1", "p-1", 2);

        assertEquals(5, item.getAvailableQuantity());
        verify(inventoryReservationRepository).deleteById("order-1");
        verify(inventoryEventProducer).publishReleased("order-1");
        verify(processedEventRepository).save(any(ProcessedEvent.class));
    }

    @Test
    void duplicateReleaseEventDoesNotRestoreAgain() {
        when(processedEventRepository.existsById("cancel-1")).thenReturn(true);

        inventoryService.release("cancel-1", "order-1", "p-1", 2);

        verifyNoInteractions(inventoryRepository, inventoryReservationRepository, inventoryEventProducer, inventoryCacheService);
    }
}
