



package net.mingsoft.mdiy.dao;

import java.util.List;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import net.mingsoft.base.dao.IBaseDao;
import net.mingsoft.mdiy.entity.DictEntity;

/**
 * 字典表持久层
 * @version
 * 版本号：1.0.0<br/>
 * 创建日期：2016-9-8 17:11:19<br/>
 * 历史修订：<br/>
 */
public interface IDictDao extends IBaseDao<DictEntity> {

    List<DictEntity> dictType(DictEntity dictEntity);

    /**
     * 使用站群排除appId拼接问题
     * @param dictEntity
     * @return
     */
    //@SqlParser(filter = true)
    @InterceptorIgnore(tenantLine = "true")
    List<DictEntity> queryExcludeApp(DictEntity dictEntity);

}
