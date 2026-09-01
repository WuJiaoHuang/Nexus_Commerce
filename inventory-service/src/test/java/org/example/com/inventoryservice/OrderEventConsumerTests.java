package org.example.com.inventoryservice;

import org.example.com.inventoryservice.kafka.OrderEventConsumer;
import org.example.com.inventoryservice.service.InventoryService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class OrderEventConsumerTests {

    @Test
    void orderCreatedDelegatesToInventoryService() {
        InventoryService inventoryService = mock(InventoryService.class);
        OrderEventConsumer consumer = new OrderEventConsumer(inventoryService, false);

        consumer.onOrderCreated("eventId=event-1;orderId=order-1;productId=product-1;quantity=2");

        verify(inventoryService).reserve("event-1", "order-1", "product-1", 2);
    }

    @Test
    void forcedFailureThrowsBeforeInventoryMutationWhenEnabled() {
        InventoryService inventoryService = mock(InventoryService.class);
        OrderEventConsumer consumer = new OrderEventConsumer(inventoryService, true);

        assertThrows(
                IllegalStateException.class,
                () -> consumer.onOrderCreated(
                        "eventId=event-1;orderId=order-1;productId=product-1;quantity=2;forceFailure=true"
                )
        );
        verifyNoInteractions(inventoryService);
    }
}
