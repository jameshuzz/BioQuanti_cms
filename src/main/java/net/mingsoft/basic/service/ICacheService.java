
package net.mingsoft.basic.service;


/**
 * 缓存业务接口门面
 * @see net.mingsoft.config.CacheConfig
 */
public interface ICacheService {

    Object get(String key);

    <T> T get(String key, Class<T> clazz);

    default <T> T get(String prefix, String key, Class<T> clazz) {
        return get(buildKey(prefix, key), clazz);
    }

    void put(String key,Object value);

    default void put(String prefix, String key, Object value) {
        put(buildKey(prefix, key), value);
    }

    /**
     * @param ttl 单位ms
     */
    void put(String key,Object value,long ttl);

    /**
     * 带有类别的缓存设置
     * @param prefix 一般为缓存业务类别 type，用 : 连接, prefix:key
     * @param key key
     * @param value value
     * @param ttl 过期时间 ms
     */
    default void put(String prefix, String key, Object value, long ttl) {
        put(buildKey(prefix, key), value, ttl);
    }

    void remove(String key);

    default void remove(String prefix,String key) {
        remove(buildKey(prefix,key));
    }

    boolean exists(String key);

    default boolean exists(String prefix,String key) {
        return exists(buildKey(prefix,key));
    }

    default String buildKey(String prefix, String key) {
        return prefix + ":" + key;
    }

}
