package org.example.com.productservice.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.com.productservice.pojo.Product;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

@Service
public class ProductCacheService {

    private static final String NULL_VALUE = "__NULL__";
    private static final String PRODUCT_KEY_PREFIX = "product:detail:";
    private static final String USER_PRODUCTS_KEY_PREFIX = "product:user:";
    private static final String LOCK_KEY_PREFIX = "lock:";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final Duration productTtl;
    private final Duration emptyTtl;
    private final Duration lockTtl;

    public ProductCacheService(StringRedisTemplate redisTemplate,
                               ObjectMapper objectMapper,
                               @Value("${app.cache.product-ttl-seconds:300}") long productTtlSeconds,
                               @Value("${app.cache.empty-ttl-seconds:60}") long emptyTtlSeconds,
                               @Value("${app.cache.lock-ttl-seconds:3}") long lockTtlSeconds) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.productTtl = Duration.ofSeconds(productTtlSeconds);
        this.emptyTtl = Duration.ofSeconds(emptyTtlSeconds);
        this.lockTtl = Duration.ofSeconds(lockTtlSeconds);
    }

    public Product getProduct(String productId, Supplier<Product> dbLoader) {
        String key = PRODUCT_KEY_PREFIX + productId;
        String cached = redisTemplate.opsForValue().get(key);
        if (NULL_VALUE.equals(cached)) {
            throw new IllegalArgumentException("Product not found: " + productId);
        }
        if (cached != null) {
            return readProduct(cached);
        }

        Optional<Product> loaded = withHotKeyLock(key, () -> Optional.ofNullable(dbLoader.get()));
        if (loaded.isEmpty()) {
            redisTemplate.opsForValue().set(key, NULL_VALUE, emptyTtl);
            throw new IllegalArgumentException("Product not found: " + productId);
        }
        cacheProduct(loaded.get());
        return loaded.get();
    }

    public List<Product> getProductsByUser(String userId, Supplier<List<Product>> dbLoader) {
        String key = USER_PRODUCTS_KEY_PREFIX + userId;
        String cached = redisTemplate.opsForValue().get(key);
        if (cached != null) {
            return readProducts(cached);
        }
        List<Product> products = dbLoader.get();
        writeJson(key, products, ttlWithJitter(productTtl));
        return products;
    }

    public void cacheProduct(Product product) {
        writeJson(PRODUCT_KEY_PREFIX + product.getId(), product, ttlWithJitter(productTtl));
    }

    public void evictUserProducts(String userId) {
        redisTemplate.delete(USER_PRODUCTS_KEY_PREFIX + userId);
    }

    private Optional<Product> withHotKeyLock(String key, Supplier<Optional<Product>> dbLoader) {
        String lockKey = LOCK_KEY_PREFIX + key;
        Boolean locked = redisTemplate.opsForValue().setIfAbsent(lockKey, "1", lockTtl);
        if (Boolean.TRUE.equals(locked)) {
            try {
                return dbLoader.get();
            } finally {
                redisTemplate.delete(lockKey);
            }
        }

        try {
            Thread.sleep(50L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        String cached = redisTemplate.opsForValue().get(key);
        if (cached == null || NULL_VALUE.equals(cached)) {
            return Optional.empty();
        }
        return Optional.of(readProduct(cached));
    }

    private Product readProduct(String value) {
        try {
            return objectMapper.readValue(value, Product.class);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to read cached product", e);
        }
    }

    private List<Product> readProducts(String value) {
        try {
            return objectMapper.readValue(value, new TypeReference<>() {
            });
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to read cached product list", e);
        }
    }

    private void writeJson(String key, Object value, Duration ttl) {
        try {
            redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(value), ttl);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to write product cache", e);
        }
    }

    private Duration ttlWithJitter(Duration ttl) {
        long jitter = ThreadLocalRandom.current().nextLong(0, 60);
        return ttl.plusSeconds(jitter);
    }
}
