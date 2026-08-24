






package net.mingsoft.basic.biz;

import net.mingsoft.base.biz.IBaseBiz;
import net.mingsoft.basic.entity.LogEntity;


/**
 * 系统日志业务
 * 创建日期：2020-11-21 9:41:34<br/>
 * 历史修订：<br/>
 */
public interface ILogBiz extends IBaseBiz<LogEntity> {
    /**
     * 异步保存数据
     * @param logEntity
     * @throws InterruptedException
     */
    void saveData(LogEntity logEntity) throws InterruptedException;
}
