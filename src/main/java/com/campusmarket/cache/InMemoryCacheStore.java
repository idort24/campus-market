package com.campusmarket.cache;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 内存版缓存实现（默认兜底）
 *
 * 生效条件：cache.store=memory 或不配（默认）。
 * RedisCacheStore 与之互斥生效，同一时刻容器里只有一个 CacheStore 实现，
 * 业务代码按接口注入，不需要 @Primary 也不会有多个候选冲突。
 */
@Component
@ConditionalOnProperty(name = "cache.store", havingValue = "memory", matchIfMissing = true)
public class InMemoryCacheStore implements CacheStore {

    /** 缓存条目：value + 过期时间戳（毫秒），0 表示不过期 */
    private static class Entry {
        final String value;
        final long expireAt;

        Entry(String value, long expireAt) {
            this.value = value;
            this.expireAt = expireAt;
        }

        boolean expired() {
            return expireAt > 0 && System.currentTimeMillis() > expireAt;
        }
    }

    private final Map<String, Entry> store = new ConcurrentHashMap<>();

    @Override
    public void set(String key, String value, long ttlSeconds) {
        if (key == null || value == null) {
            return;
        }
        long expireAt = ttlSeconds > 0 ? System.currentTimeMillis() + ttlSeconds * 1000 : 0;
        store.put(key, new Entry(value, expireAt));
    }

    @Override
    public String get(String key) {
        if (key == null) {
            return null;
        }
        Entry e = store.get(key);
        if (e == null) {
            return null;
        }
        // 惰性删除：读到过期数据时顺手清理，避免内存无限增长
        if (e.expired()) {
            store.remove(key, e);
            return null;
        }
        return e.value;
    }

    @Override
    public void delete(String key) {
        if (key != null) {
            store.remove(key);
        }
    }

    @Override
    public boolean exists(String key) {
        return get(key) != null;
    }
}
