



package net.mingsoft.mdiy.biz;

import net.mingsoft.base.biz.IBaseBiz;
import net.mingsoft.mdiy.entity.TagEntity;

import java.util.List;

/**
 * 标签业务
 * 创建日期：2018-10-24 8:44:34<br/>
 * 历史修订：<br/>
 */
public interface ITagBiz extends IBaseBiz<TagEntity> {

    /**
     * 用于获取缓存,增加效率
     */
    List<TagEntity> queryAll(TagEntity tag);
}
