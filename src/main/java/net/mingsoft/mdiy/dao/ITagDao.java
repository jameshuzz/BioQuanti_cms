



package net.mingsoft.mdiy.dao;

import net.mingsoft.base.dao.IBaseDao;
import net.mingsoft.mdiy.entity.TagEntity;
import org.apache.ibatis.annotations.CacheNamespace;
import org.mybatis.caches.ehcache.EhcacheCache;

import java.util.List;

/**
 * 标签持久层
 * 创建日期：2018-10-24 8:44:34<br/>
 * 历史修订：<br/>
 */
public interface ITagDao extends IBaseDao<TagEntity> {

    List<TagEntity> queryAll(TagEntity tagEntity);

}
