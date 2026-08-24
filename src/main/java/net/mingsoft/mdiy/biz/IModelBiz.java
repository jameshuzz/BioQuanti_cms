



package net.mingsoft.mdiy.biz;

import net.mingsoft.base.biz.IBaseBiz;
import net.mingsoft.mdiy.bean.ModelJsonBean;
import net.mingsoft.mdiy.entity.ModelEntity;

import java.io.Serializable;
import java.util.List;


/**
 * 自定义表单接口
 * @author 铭软团队
 * @version
 * 版本号：100-000-000<br/>
 * 创建日期：2012-03-15<br/>
 * 历史修订：<br/>
 */
public interface IModelBiz extends IBaseBiz<ModelEntity> {

    /**
     * 导入模型，提供自定义配置和自定义表单使用
     * @param customType 自定义类型（表单、配置）
     * @param modelJsonBean 来自代码生成器的自定义模型json转换成的bean
     * @return
     */
    boolean importConfig(String customType, ModelJsonBean modelJsonBean);

    /**
     * 导入模型，提供自定义模型
     * @param customType 自定义类型（模型）
     * @param modelJsonBean 来自代码生成器的自定义模型json转换成的bean
     * @param modelType 自定义模型类型
     * @return
     */
    boolean importModel(String customType, ModelJsonBean modelJsonBean,String modelType);

    /**
     * 执行模型sql
     * @param prefix 表前缀
     * @param modelJsonBean 自定义模型json转换成的bean
     */
    void executeModelSql(String prefix,ModelJsonBean modelJsonBean);


    /**
     * 更新导入模型，提供自定义模型和自定义表单使用
     * @param modelId 自定义模型编号
     * @param modelJsonBean 来自代码生成器的自定义模型json转换成的bean
     * @return
     */
    boolean updateConfig(String modelId, ModelJsonBean modelJsonBean);

    /**
     * 更新导入模型，提供自定义模型和自定义表单使用
     * @param modelId 自定义模型编号
     * @param modelJsonBean 来自代码生成器的自定义模型json转换成的bean
     * @param modelType 自定义模型类型，导入模型时候下拉选择的业务类型，如：文章类型，只能在内容管理业务使用
     * @return
     */
    boolean updateConfig(String modelId, ModelJsonBean modelJsonBean,String modelType);

    /**
     * 批量删除，并且删除表
     * @param ids
     * @return
     */
    boolean delete (List<String> ids);

    /**
     * 更新模型字段(公共方法抽取，不提供给控制层使用)
     * * @param modelEntity 自定义模型
     * * @param modelJsonBean 来自代码生成器的自定义模型json转换成的bean
     * * @param modelType 自定义模型类型，导入模型时候下拉选择的业务类型，如：文章类型，只能在内容管理业务使用
     * * @return
     */
    void updateModelField(ModelJsonBean modelJsonBean,ModelEntity modelEntity,String modelType);

    /**
     * 查询模型数据，如果在站群环境下，则只会查询当前站点下的模型数据<br>
     * 注意，此方法忽略多租户，如需要管理，请使用list();
     * @param modelEntity 自定义模型查询条件
     * @return 模型集合
     */
    List<ModelEntity> query(ModelEntity modelEntity);

    /**
     * 根据自定义模型查询，如果在站群环境下，则只会查询当前站点下的模型数据
     * @param modelEntity 自定义模型查询条件， <br>
     *                    modelCustomType(必填)：自定义模型类型，如CONFIG、MODEL、FORM<br>
     *                    modelName(选填): 模型名称，和模型编号必填一个<br>
     *                    id(选填)：模型编号，和模型名称必填一个，如果有模型id,建议直接使用getEntityById(),不然依旧会拼appId<br>
     * @return 返回模型实体， 会返回null,需要判断处理
     */
    ModelEntity getByEntity(ModelEntity modelEntity);

    /**
     * 根据模型id查询模型实体<br>
     * 非站群下：会根据id查询<br>
     * 站群下：如果当前站点是主站点，那么根据id查询，如果当前站点是子站点，那么会根据id+appId查询，防止子站点越权删除全局模型<br>
     * @param id 自定义模型id
     * @return 模型实体
     */
    ModelEntity getById(Serializable id);

    /**
     * 根据模型id查询模型实体<br>
     * 防止有些特殊情况，子站点需要查询全局模型，所以提供此方法<br>
     * 注意，此方法不进行权限控制，请谨慎使用，建议只涉及到查询不涉及增删改操作
     * @param id 自定义模型id
     * @return 模型实体
     */
    ModelEntity getEntityById(String id);

    /**
     * 更新自定义模型
     * @param modelEntity 自定义模型实体<br>
     *                    id(必填)：自定义模型编号
     * @return 是否更新成功
     */
    boolean updateById(ModelEntity modelEntity);
}
