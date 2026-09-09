package org.example.com.inventoryservice.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@TableName("inventory")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryItem {
    @TableId(value = "product_id", type = IdType.INPUT)
    private String productId;
    private Integer availableQuantity;
}
