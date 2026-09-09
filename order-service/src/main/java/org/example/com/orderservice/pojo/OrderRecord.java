package org.example.com.orderservice.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@TableName("orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderRecord {
    @TableId(value = "id", type = IdType.INPUT)
    private String id;
    private String userId;
    private String productId;
    private Integer quantity;
    private String status;
    private LocalDateTime createdAt;
}
