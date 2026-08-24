



package net.mingsoft.mdiy.biz.impl;

import net.mingsoft.base.biz.impl.BaseBizImpl;
import net.mingsoft.base.dao.IBaseDao;
import net.mingsoft.mdiy.biz.ITagBiz;
import net.mingsoft.mdiy.dao.ITagDao;
import net.mingsoft.mdiy.entity.TagEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 标签管理持久化层
 * 创建日期：2018-10-24 8:44:34<br/>
 * 历史修订：<br/>
 */
@Service("tagBizImpl")
@Transactional(rollbackFor = RuntimeException.class)
public class TagBizImpl extends BaseBizImpl<ITagDao, TagEntity> implements ITagBiz {

    @Autowired
    private ITagDao tagDao;

    @Override
    protected IBaseDao getDao() {
        
        return tagDao;
    }

    @Override
    public List<TagEntity> queryAll(TagEntity tagEntity) {
        return tagDao.queryAll(tagEntity);
    }

}
