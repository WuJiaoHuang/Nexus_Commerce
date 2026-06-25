package org.example.com.inventoryservice.pojo;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "inventory_reservation")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryReservation {
    @Id
    private String orderId;
    private String productId;
    private Integer quantity;
}
