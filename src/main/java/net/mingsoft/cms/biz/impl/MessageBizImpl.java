package net.mingsoft.cms.biz.impl;

import net.mingsoft.base.biz.impl.BaseBizImpl;
import net.mingsoft.base.dao.IBaseDao;
import net.mingsoft.cms.biz.IMessageBiz;
import net.mingsoft.cms.dao.IMessageDao;
import net.mingsoft.cms.entity.MessageEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 客户留言业务实现层
 * 创建日期：2026-08-24<br/>
 * 历史修订：<br/>
 */
@Service("cmsMessageBizImpl")
public class MessageBizImpl extends BaseBizImpl<IMessageDao, MessageEntity> implements IMessageBiz {

    @Autowired
    private IMessageDao messageDao;

    @Override
    protected IBaseDao getDao() {
        return messageDao;
    }
}
