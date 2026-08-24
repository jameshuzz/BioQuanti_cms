






package net.mingsoft.basic.cache;

import cn.hutool.core.collection.CollUtil;
import net.mingsoft.basic.util.SpringUtil;
import net.sf.ehcache.Ehcache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;

import java.util.List;

/**
 * 基础缓存,基于Springboot的缓存机制
 *
 * @version 版本号：<br/>
 * 创建日期：2016年6月2日<br/>
 * 历史修订：<br/>
 */
public abstract class BaseCache<T> {

    @Autowired
    protected CacheManager cacheManager;


    /**
     * 保存时候需要指定key
     * @param t
     * @return
     */
    public abstract T saveOrUpdate(String key,T t);

    public abstract void delete(String key);

    public abstract void deleteAll();

    public abstract T get(String key);

    public abstract void flush();
    public void flush(String cacheName) {
        Ehcache cache = (Ehcache) SpringUtil.getBean(CacheManager.class).getCache(cacheName).getNativeCache();
        cache.flush();
    }

    public abstract int count();

    public int count(String cacheName) {
        Ehcache cache = (Ehcache) SpringUtil.getBean(CacheManager.class).getCache(cacheName).getNativeCache();
        return cache.getSize();
    }

    public abstract List list();

    /**
     *
     * @param cacheName
     * @return
     */
    public List list(String cacheName) {
        Ehcache cache = (Ehcache) SpringUtil.getBean(CacheManager.class).getCache(cacheName).getNativeCache();
        List list = CollUtil.newArrayList();
        cache.getKeys().forEach(key ->
                list.add(cacheManager.getCache(cacheName).get(key).get())
        );
        return list;
    }

}

