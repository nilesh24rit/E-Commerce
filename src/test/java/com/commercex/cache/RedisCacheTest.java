package com.commercex.cache;

import com.commercex.config.RedisConfig;
import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;

import static org.junit.jupiter.api.Assertions.*;

class RedisCacheTest {

    @Test
    void testCachePutAndEvict_Operations() {
        CacheManager cacheManager = new ConcurrentMapCacheManager("products", "categories", "coupons");
        
        Cache productsCache = cacheManager.getCache("products");
        assertNotNull(productsCache);

        // Put item into cache
        productsCache.put("prod-1", "Laptop");
        assertEquals("Laptop", productsCache.get("prod-1", String.class));

        // Evict item from cache
        productsCache.evict("prod-1");
        assertNull(productsCache.get("prod-1"));
    }

    @Test
    void testCacheClear_Operations() {
        CacheManager cacheManager = new ConcurrentMapCacheManager("categories");
        Cache categoriesCache = cacheManager.getCache("categories");
        assertNotNull(categoriesCache);

        categoriesCache.put("cat-1", "Electronics");
        categoriesCache.put("cat-2", "Books");

        categoriesCache.clear();
        assertNull(categoriesCache.get("cat-1"));
        assertNull(categoriesCache.get("cat-2"));
    }
}
