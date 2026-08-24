



package net.mingsoft.mdiy.biz;

import net.mingsoft.base.biz.IBaseBiz;
import net.mingsoft.mdiy.bean.ModelJsonBean;
import net.mingsoft.mdiy.entity.ConfigEntity;

import java.util.List;


/**
 * 自定义配置业务
 * @author SMILE
 * 创建日期：2021-3-25 11:42:09<br/>
 * 历史修订：<br/>
 */
public interface IConfigBiz extends IBaseBiz<ConfigEntity> {

    /**
     * 特殊处理，因为会和自定义标签导入模型是表名重复，这边处理自定义配置表名重复问题
     * 目前是把表名字段设置成null
     * 导入自定义模型，提供自定义配置和全局标签使用
     * @param customType 自定义类型
     * @param modelJsonBean 来自代码生成器的自定义配置json转换成的bean
     */
    boolean importConfig(String customType, ModelJsonBean modelJsonBean);

    /**
     * 更新导入配置，提供自定义配置使用
     * @param modelId 自定义配置编号
     * @param modelJsonBean 来自代码生成器的自定义配置json转换成的bean
     * @return
     */
    boolean updateConfig(String modelId, ModelJsonBean modelJsonBean);

    /**
     * 批量删除自定义配置
     * @param ids 自定义配置编号集合
     * @return boolean true：删除成功，false：删除失败
     */
    boolean delete(List<String> ids);

    /**
     * 根据自定义配置实体获取自定义配置实体<br>
     * 建议使用环境站群环境下，如其他建议使用getOne()
     * @param configEntity 自定义配置实体<br>
     *                     configName(必填)：自定义配置名称<br>
     *                     configType(必填)：自定义配置类型<br>
     *                     appId(选填)：站点id，不传入查询全局配置，传入则查询当前appId的配置
     * @return 自定义配置实体
     */
    ConfigEntity getByEntity(ConfigEntity configEntity);

    /**
     * 重新加载缓存
     */
    void reloadCache();

}
