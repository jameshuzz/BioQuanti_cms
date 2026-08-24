
package net.mingsoft.basic.service;

import cn.hutool.cache.Cache;
import cn.hutool.cache.CacheUtil;

/**
 * 本地内存缓存实现类（基于Hutool 定时缓存），适用于单机环境
 */
public class LocalCacheService implements ICacheService {

    private final static Cache<String,Object> cache = CacheUtil.newLFUCache(0);

    @Override
    public Object get(String key) {
        return cache.get(key);
    }

    @Override
    public <T> T get(String key, Class<T> clazz) {
        Object value = cache.get(key);
        if (value == null) {
            return null;
        }
        if (clazz.isInstance(value)) {
            return clazz.cast(value);
        }
        return null;
    }

    @Override
    public void put(String key, Object value) {
        cache.put(key,value);
    }

    @Override
    public void put(String key, Object value, long ttl) {
        cache.put(key,value,ttl);
    }

    @Override
    public void remove(String key) {
        cache.remove(key);
    }

    @Override
    public boolean exists(String key) {
        return cache.containsKey(key);
    }
}
