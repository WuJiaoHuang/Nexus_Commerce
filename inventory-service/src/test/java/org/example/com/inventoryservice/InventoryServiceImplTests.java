package org.example.com.inventoryservice;

import org.example.com.inventoryservice.cache.InventoryCacheService;
import org.example.com.inventoryservice.kafka.InventoryEventProducer;
import org.example.com.inventoryservice.mapper.InventoryMapper;
import org.example.com.inventoryservice.mapper.InventoryReservationMapper;
import org.example.com.inventoryservice.mapper.ProcessedEventMapper;
import org.example.com.inventoryservice.pojo.InventoryItem;
import org.example.com.inventoryservice.pojo.InventoryReservation;
import org.example.com.inventoryservice.pojo.ProcessedEvent;
import org.example.com.inventoryservice.service.impl.InventoryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceImplTests {

    @Mock
    InventoryMapper inventoryMapper;

    @Mock
    InventoryReservationMapper inventoryReservationMapper;

    @Mock
    ProcessedEventMapper processedEventMapper;

    @Mock
    InventoryEventProducer inventoryEventProducer;

    @Mock
    InventoryCacheService inventoryCacheService;

    InventoryServiceImpl inventoryService;

    @BeforeEach
    void setUp() {
        inventoryService = new InventoryServiceImpl(
                inventoryMapper,
                inventoryReservationMapper,
                processedEventMapper,
                inventoryEventProducer,
                inventoryCacheService);
    }

    @Test
    void reserveDecrementsInventoryAndPublishesReserved() {
        InventoryItem item = new InventoryItem("p-1", 5);
        when(processedEventMapper.selectById("event-1")).thenReturn(null);
        when(inventoryReservationMapper.selectById("order-1")).thenReturn(null);
        when(inventoryMapper.selectById("p-1")).thenReturn(item);

        inventoryService.reserve("event-1", "order-1", "p-1", 2);

        assertEquals(3, item.getAvailableQuantity());
        verify(inventoryMapper).updateById(item);
        verify(inventoryReservationMapper).insert(new InventoryReservation("order-1", "p-1", 2));
        verify(inventoryEventProducer).publishReserved("order-1");
        verify(processedEventMapper).insert(any(ProcessedEvent.class));
    }

    @Test
    void reserveRejectsWhenInventoryIsInsufficient() {
        InventoryItem item = new InventoryItem("p-1", 1);
        when(processedEventMapper.selectById("event-1")).thenReturn(null);
        when(inventoryReservationMapper.selectById("order-1")).thenReturn(null);
        when(inventoryMapper.selectById("p-1")).thenReturn(item);

        inventoryService.reserve("event-1", "order-1", "p-1", 2);

        assertEquals(1, item.getAvailableQuantity());
        verify(inventoryEventProducer).publishRejected("order-1", "OUT_OF_STOCK");
        verify(inventoryMapper, never()).updateById(any(InventoryItem.class));
        verify(processedEventMapper).insert(any(ProcessedEvent.class));
    }

    @Test
    void duplicateReserveEventDoesNotDecrementAgain() {
        when(processedEventMapper.selectById("event-1")).thenReturn(new ProcessedEvent());

        inventoryService.reserve("event-1", "order-1", "p-1", 2);

        verifyNoInteractions(inventoryMapper, inventoryReservationMapper, inventoryEventProducer, inventoryCacheService);
    }

    @Test
    void releaseRestoresInventoryAndPublishesReleased() {
        InventoryReservation reservation = new InventoryReservation("order-1", "p-1", 2);
        InventoryItem item = new InventoryItem("p-1", 3);
        when(processedEventMapper.selectById("cancel-1")).thenReturn(null);
        when(inventoryReservationMapper.selectById("order-1")).thenReturn(reservation);
        when(inventoryMapper.selectById("p-1")).thenReturn(item);

        inventoryService.release("cancel-1", "order-1", "p-1", 2);

        assertEquals(5, item.getAvailableQuantity());
        verify(inventoryMapper).updateById(item);
        verify(inventoryReservationMapper).deleteById("order-1");
        verify(inventoryEventProducer).publishReleased("order-1");
        verify(processedEventMapper).insert(any(ProcessedEvent.class));
    }

    @Test
    void duplicateReleaseEventDoesNotRestoreAgain() {
        when(processedEventMapper.selectById("cancel-1")).thenReturn(new ProcessedEvent());

        inventoryService.release("cancel-1", "order-1", "p-1", 2);

        verifyNoInteractions(inventoryMapper, inventoryReservationMapper, inventoryEventProducer, inventoryCacheService);
    }
}
