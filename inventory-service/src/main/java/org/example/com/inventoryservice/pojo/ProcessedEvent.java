package org.example.com.inventoryservice.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@TableName("processed_event")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProcessedEvent {
    @TableId(value = "event_id", type = IdType.INPUT)
    private String eventId;
    private String eventType;
    private String aggregateId;
    private LocalDateTime processedAt;
}
