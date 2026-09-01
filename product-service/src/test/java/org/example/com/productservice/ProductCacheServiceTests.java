package org.example.com.productservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.com.productservice.cache.ProductCacheService;
import org.example.com.productservice.pojo.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductCacheServiceTests {

    @Mock
    StringRedisTemplate redisTemplate;

    @Mock
    ValueOperations<String, String> valueOperations;

    ProductCacheService cacheService;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        cacheService = new ProductCacheService(redisTemplate, new ObjectMapper(), 300, 60, 3);
    }

    @Test
    void cacheHitReturnsCachedProductWithoutDatabaseLookup() throws Exception {
        Product cachedProduct = product("p-1");
        when(valueOperations.get("product:detail:p-1"))
                .thenReturn(new ObjectMapper().writeValueAsString(cachedProduct));

        Product result = cacheService.getProduct("p-1", () -> {
            fail("database loader should not be called on cache hit");
            return null;
        });

        assertEquals("p-1", result.getId());
    }

    @Test
    void cacheMissLoadsFromDatabaseAndWritesRedis() {
        when(valueOperations.get("product:detail:p-1")).thenReturn(null);
        when(valueOperations.setIfAbsent(eq("lock:product:detail:p-1"), eq("1"), any())).thenReturn(true);
        AtomicBoolean loaded = new AtomicBoolean(false);

        Product result = cacheService.getProduct("p-1", () -> {
            loaded.set(true);
            return product("p-1");
        });

        assertTrue(loaded.get());
        assertEquals("p-1", result.getId());
        verify(valueOperations).set(eq("product:detail:p-1"), contains("\"id\":\"p-1\""), any());
        verify(redisTemplate).delete("lock:product:detail:p-1");
    }

    @Test
    void cacheMissWithMissingProductWritesEmptyValue() {
        when(valueOperations.get("product:detail:missing")).thenReturn(null);
        when(valueOperations.setIfAbsent(eq("lock:product:detail:missing"), eq("1"), any())).thenReturn(true);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> cacheService.getProduct("missing", () -> null));

        assertTrue(exception.getMessage().contains("Product not found"));
        verify(valueOperations).set(eq("product:detail:missing"), eq("__NULL__"), any());
    }

    private Product product(String id) {
        Product product = new Product();
        product.setId(id);
        product.setName("Keyboard");
        product.setPrice(BigDecimal.TEN);
        product.setStock(10);
        return product;
    }
}
