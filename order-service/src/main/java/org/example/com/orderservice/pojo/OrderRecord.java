package org.example.com.orderservice.pojo;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderRecord {
    @Id
    private String id;
    private String userId;
    private String productId;
    private Integer quantity;
    private String status;
    private LocalDateTime createdAt;
}
