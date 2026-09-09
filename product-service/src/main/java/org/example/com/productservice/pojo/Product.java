package org.example.com.productservice.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@TableName("product")
@NoArgsConstructor
public class Product {
    @TableId(value = "id", type = IdType.INPUT)
    private String id;

    private String name;

    private BigDecimal price;

    private Integer stock;

    private String description;

}
