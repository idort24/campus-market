package com.campusmarket.cache;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Redis 版缓存实现
 *
 * 生效条件：application.yaml 里 cache.store=redis
 * 不配或配 memory 时本类不实例化，走 InMemoryCacheStore——
 * 答辩现场 Redis 没起来，整个登录/验证码也不会挂。
 *
 * 为什么用 StringRedisTemplate：
 * CacheStore 的 value 定义就是 String（验证码答案、黑名单 token、字典 JSON）。
 * StringRedisTemplate 的 key/value 都用 String 序列化器，
 * 存 "8" 取出来就是 "8"；不要用 GenericJackson2JsonRedisSerializer，
 * 否则 String 会被包成 LinkedHashMap，验证码比对直接挂。
 */
@Component
@ConditionalOnProperty(name = "cache.store", havingValue = "redis")
public class RedisCacheStore implements CacheStore {

    private final StringRedisTemplate redisTemplate;

    public RedisCacheStore(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void set(String key, String value, long ttlSeconds) {
        if (key == null || value == null) {
            return;
        }
        if (ttlSeconds > 0) {
            // 验证码 60s、黑名单 token 剩余有效期，都走这里
            redisTemplate.opsForValue().set(key, value, ttlSeconds, TimeUnit.SECONDS);
        } else {
            // 字典静态数据不设 TTL，吴老师讲的"一次加载多次使用"
            redisTemplate.opsForValue().set(key, value);
        }
    }

    @Override
    public String get(String key) {
        if (key == null) {
            return null;
        }
        return redisTemplate.opsForValue().get(key);
    }

    @Override
    public void delete(String key) {
        if (key != null) {
            redisTemplate.delete(key);
        }
    }

    @Override
    public boolean exists(String key) {
        if (key == null) {
            return false;
        }
        Boolean has = redisTemplate.hasKey(key);
        return Boolean.TRUE.equals(has);
    }
}
