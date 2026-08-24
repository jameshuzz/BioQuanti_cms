



package net.mingsoft.mdiy.dao;

import net.mingsoft.base.dao.IBaseDao;
import net.mingsoft.mdiy.entity.ConfigEntity;
import org.springframework.stereotype.Component;

/**
 * 自定义配置持久层
 * 创建日期：2020-5-28 15:12:02<br/>
 * 历史修订：<br/>
 */
@Component("mdiyCoConfigDao")
public interface IConfigDao extends IBaseDao<ConfigEntity> {

    ConfigEntity getByConfigName(String configName);

    /**
     * 根据自定义配置实体获取自定义配置实体<br>
     * 建议使用环境站群环境下，如其他建议使用selectOne()
     * @param configEntity 自定义配置实体<br>
     *                     configName(必填)：自定义配置名称<br>
     *                     configType(必填)：自定义配置类型<br>
     *                     appId(选填)：站点id，不传入查询全局配置，传入则查询当前appId的配置
     * @return 自定义配置实体
     */
    ConfigEntity getByEntity(ConfigEntity configEntity);
}
