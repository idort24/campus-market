package com.campusmarket.cache;

/**
 * 统一缓存抽象层
 *
 * 设计目的：
 * 1. 业务代码（验证码、token 黑名单等）只依赖本接口，不直接依赖 Redis API；
 * 2. 当前用内存实现 InMemoryCacheStore 保证开发期可跑；
 * 3. 周泓颖接入 Redis 后，只需新增一个 RedisCacheStore 并加 @Primary，
 *    本接口的实现自动切换，其他代码零改动。
 */
public interface CacheStore {

    /**
     * 写入缓存并设置过期时间
     *
     * @param key        缓存键
     * @param value      缓存值（统一用字符串，复杂对象自行 JSON 序列化）
     * @param ttlSeconds 过期秒数，<= 0 表示不过期
     */
    void set(String key, String value, long ttlSeconds);

    /** 读取缓存，不存在或已过期返回 null */
    String get(String key);

    /** 删除缓存 */
    void delete(String key);

    /** 判断键是否存在且未过期 */
    boolean exists(String key);
}
