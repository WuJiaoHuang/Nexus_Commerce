package org.example.com.inventoryservice.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.com.inventoryservice.pojo.InventoryItem;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

@Service
public class InventoryCacheService {

    private static final String INVENTORY_KEY_PREFIX = "inventory:product:";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final Duration inventoryTtl;

    public InventoryCacheService(StringRedisTemplate redisTemplate,
                                 ObjectMapper objectMapper,
                                 @Value("${app.cache.inventory-ttl-seconds:120}") long inventoryTtlSeconds) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.inventoryTtl = Duration.ofSeconds(inventoryTtlSeconds);
    }

    public InventoryItem getInventory(String productId, Supplier<InventoryItem> dbLoader) {
        String key = INVENTORY_KEY_PREFIX + productId;
        String cached = redisTemplate.opsForValue().get(key);
        if (cached != null) {
            return readInventory(cached);
        }
        InventoryItem item = dbLoader.get();
        cacheInventory(item);
        return item;
    }

    public void cacheInventory(InventoryItem item) {
        try {
            redisTemplate.opsForValue().set(INVENTORY_KEY_PREFIX + item.getProductId(),
                    objectMapper.writeValueAsString(item), ttlWithJitter());
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to write inventory cache", e);
        }
    }

    public void evictInventory(String productId) {
        redisTemplate.delete(INVENTORY_KEY_PREFIX + productId);
    }

    private InventoryItem readInventory(String value) {
        try {
            return objectMapper.readValue(value, InventoryItem.class);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to read inventory cache", e);
        }
    }

    private Duration ttlWithJitter() {
        return inventoryTtl.plusSeconds(ThreadLocalRandom.current().nextLong(0, 30));
    }
}
