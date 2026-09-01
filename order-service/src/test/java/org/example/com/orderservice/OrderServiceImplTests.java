package org.example.com.orderservice;

import org.example.com.orderservice.dao.OrderRepository;
import org.example.com.orderservice.impl.OrderServiceImpl;
import org.example.com.orderservice.kafka.InventoryResultConsumer;
import org.example.com.orderservice.kafka.OrderEventProducer;
import org.example.com.orderservice.pojo.OrderRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTests {

    @Mock
    OrderRepository orderRepository;

    @Mock
    OrderEventProducer orderEventProducer;

    @Test
    void createOrderPersistsCreatedStatusAndPublishesEvent() {
        OrderRecord input = order("order-1", "p-1", 2, null);
        when(orderRepository.save(input)).thenReturn(input);
        OrderServiceImpl orderService = new OrderServiceImpl(orderRepository, orderEventProducer);

        OrderRecord result = orderService.createOrder(input);

        assertEquals("CREATED", result.getStatus());
        assertNotNull(result.getCreatedAt());
        verify(orderEventProducer).publishOrderCreated(input);
    }

    @Test
    void inventoryEventsUpdateOrderStatus() {
        OrderRecord order = order("order-1", "p-1", 2, "CREATED");
        when(orderRepository.findById("order-1")).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);
        OrderServiceImpl orderService = new OrderServiceImpl(orderRepository, orderEventProducer);
        InventoryResultConsumer consumer = new InventoryResultConsumer(orderService, "inventory-rejected");

        consumer.consumeInventoryReserved("orderId=order-1");
        assertEquals("RESERVED", order.getStatus());

        consumer.consumeInventoryRejected("orderId=order-1;reason=OUT_OF_STOCK");
        assertEquals("REJECTED:OUT_OF_STOCK", order.getStatus());

        consumer.consumeInventoryReleased("orderId=order-1");
        assertEquals("CANCELLED", order.getStatus());
    }

    @Test
    void cancelOrderMarksCancellingAndPublishesCancellation() {
        OrderRecord order = order("order-1", "p-1", 2, "RESERVED");
        when(orderRepository.findById("order-1")).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);
        OrderServiceImpl orderService = new OrderServiceImpl(orderRepository, orderEventProducer);

        OrderRecord result = orderService.cancelOrder("order-1");

        assertEquals("CANCELLING", result.getStatus());
        ArgumentCaptor<OrderRecord> captor = ArgumentCaptor.forClass(OrderRecord.class);
        verify(orderEventProducer).publishOrderCancelled(captor.capture());
        assertEquals("order-1", captor.getValue().getId());
    }

    private OrderRecord order(String id, String productId, Integer quantity, String status) {
        OrderRecord order = new OrderRecord();
        order.setId(id);
        order.setUserId("user-1");
        order.setProductId(productId);
        order.setQuantity(quantity);
        order.setStatus(status);
        return order;
    }
}
