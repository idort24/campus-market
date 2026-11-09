package com.campusmarket.cache;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 内存版缓存实现（开发期兜底）
 *
 * 说明：
 * - 单一 Spring 容器内有效，服务重启即失效；
 * - 多实例部署不共享，仅用于本机开发与联调；
 * - 周泓颖接入 Redis 后新增 RedisCacheStore 并标注 @Primary，
 *   本类会自动降级为备用实现，无需改业务代码。
 */
@Component
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
