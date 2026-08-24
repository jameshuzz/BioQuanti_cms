




package net.mingsoft.basic.util;

import cn.hutool.cache.Cache;
import cn.hutool.cache.CacheUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 简单的Map缓存实现，不依赖springboot，处理一些操作频繁且数据量较少的情况
 */
public class MapCacheUtil {
    private static Cache<String,Object> cache = CacheUtil.newLFUCache(0);
    private static final Logger LOGGER = LoggerFactory.getLogger(MapCacheUtil.class);

    /**
     * 写入对象
     * @param key 键
     * @param object 值
     * @param cover 是否覆盖
     */
    public static void put(String key, Object object,boolean cover){
        if (cache.get(key) != null){
            LOGGER.info("key: {} 的对象已存在",key);
            if (cover){
                cache.put(key,object);
                LOGGER.info("已覆盖key: {} 的对象",key);
            }
        }else {
            cache.put(key,object);
        }

    }

    public static void put(String key, Object object){
        put(key,object,true);
    }

    public static void remove(String key){
        cache.remove(key);
    }


    public static Object get(String key){
        return cache.get(key);
    }

}
