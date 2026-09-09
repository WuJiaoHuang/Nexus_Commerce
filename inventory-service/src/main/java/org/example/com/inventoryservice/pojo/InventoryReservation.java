package org.example.com.inventoryservice.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@TableName("inventory_reservation")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryReservation {
    @TableId(value = "order_id", type = IdType.INPUT)
    private String orderId;
    private String productId;
    private Integer quantity;
}
