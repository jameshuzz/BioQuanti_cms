






package net.mingsoft.basic.dao;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import net.mingsoft.base.dao.IBaseDao;
import net.mingsoft.basic.entity.LogEntity;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 系统日志持久层
 * 创建日期：2020-11-21 9:41:34<br/>
 * 历史修订：<br/>
 */
@Component("basicLogDao")
public interface ILogDao extends IBaseDao<LogEntity> {

    /**
     * 批量查询日志，注意此接口不会返回logParam、logResult和logErrorMsg字段信息，如有需要返回，建议使用 {@link #selectList(Wrapper)}方法
     * @param logEntity
     * @return
     */
    List<LogEntity> query(LogEntity logEntity);

}
