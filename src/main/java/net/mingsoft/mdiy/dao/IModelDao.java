



package net.mingsoft.mdiy.dao;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import net.mingsoft.base.dao.IBaseDao;
import java.util.*;
import net.mingsoft.mdiy.entity.ModelEntity;
import org.springframework.stereotype.Component;

/**
 * 自定义模型持久层
 * @author SMILE
 * 创建日期：2019-11-7 10:48:00<br/>
 * 历史修订：<br/>
 */
@Component("mdiyModelDao")
public interface IModelDao extends IBaseDao<ModelEntity>  {

    /**
     * 查询站点自定义模型,并且忽略多租户<br>
     * 此方法建议在站群环境下使用，正常请使用selectList()方法
     * @param modelEntity 查询条件
     * @param appId 站点id
     * @param isMasterApp 当前站点是否是主站点
     * @return 如果当前站点是主站点，返回全局配置和当前站点配置，如果不是主站点，返回当前站点的配置集合
     */
    @InterceptorIgnore(tenantLine = "true")
    List<ModelEntity> queryForSite(ModelEntity modelEntity, String appId, boolean isMasterApp);

    /**
     * 仅供分页使用
     */
    @InterceptorIgnore(tenantLine = "true")
    long queryForSite_COUNT(ModelEntity modelEntity, String appId, boolean isMasterApp);

    /**
     * 根据id查询自定义模型，忽略多租户
     * @param id 自定义模型id
     * @return 自定义模型实体
     */
    @InterceptorIgnore(tenantLine = "true")
    ModelEntity getEntityById(String id);

    /**
     * 根据自定义模型实体查询自定义模型，忽略多租户<br>
     * 此方法建议在站群环境下使用，正常请使用getOne()方法
     * @param modelEntity 自定义模型查询条件， <br>
     *                    modelCustomType(必填)：自定义模型类型，如CONFIG、MODEL、FORM<br>
     *                    modelName(选填): 模型名称，和模型编号必填一个<br>
     *                    id(选填)：模型编号，和模型名称必填一个<br>
     * @param appId 站点id
     * @param isMasterApp 是否是主站点
     * @return 自定义模型实体
     */
    @InterceptorIgnore(tenantLine = "true")
    ModelEntity getByEntity(ModelEntity modelEntity, String appId, boolean isMasterApp);

    /**
     * 根据自定义模型实体修改自定义模型，忽略多租户<br>
     * 此方法建议在站群环境下使用，正常请使用updateById()方法
     * @param modelEntity 自定义模型实体
     * @return
     */
    @InterceptorIgnore(tenantLine = "true")
    int updateEntityById(ModelEntity modelEntity);
}
