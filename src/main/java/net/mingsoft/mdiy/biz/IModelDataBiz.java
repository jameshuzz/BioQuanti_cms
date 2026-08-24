



package net.mingsoft.mdiy.biz;

import net.mingsoft.base.biz.IBaseBiz;
import net.mingsoft.base.biz.SqlQueryWrapper;
import net.mingsoft.mdiy.entity.ModelEntity;

import java.util.List;
import java.util.Map;


/**
 * 自定义模型数据
 * @author 铭软团队
 * @version
 * 版本号：100-000-000<br/>
 * 创建日期：2012-03-15<br/>
 * 历史修订：<br/>
 */
public interface IModelDataBiz extends IBaseBiz {

    /**
     * 保存自定义表单的数据<br>
     * 注意：请求体中必须有modelData数据且为字符串JSON格式。
     * modelData为JSON字符串，格式为：{"字段名":"字段值"}。其中modelData必须存在modelId字段名，如果更新模型数据，则必须存在id字段名。
     * @param linkId 关联编号
     * @return 保存成功或者更新成功都会返回true，否则返回false
     */
    boolean saveOrUpdate(String linkId);

    /**
     * 保存自定义表单的数据
     * @param modelId 模型编号
     * @param params　参数值集合
     */
    boolean saveDiyFormData(String modelId, Map<String, Object> params);

    /**
     * 保存自定义表单的数据
     * @param model 模型
     * @param params　参数值集合
     */
    boolean saveDiyFormData(ModelEntity model, Map<String, Object> params);

    /**
     * 更新自定义表单的数据
     * @param modelId 模型编号
     * @param params　参数值集合
     */
    boolean updateDiyFormData(String modelId, Map<String, Object> params);

    /**
     * 更新自定义表单的数据
     * @param model 模型
     * @param params　参数值集合
     */
    boolean updateDiyFormData(ModelEntity model, Map<String, Object> params);


    /**
     * 查询自定义表单的列表数据
     * @param modelId　模型编号
     * @param map 查询条件参数map。示例：正常查询传入{"ID":"1","NAME":"张三","ORDER":"DESC","orderBy":"ID","formFields","ID,NAME", "sqlWhere":"[]"}<br>
     *            order(String asc|desc) 排序 示例："DESC" 必须有排序字段的情况下才会生效<br>
     *            orderBy(String 字段) 排序字段 示例： "ID" <br>
     *            formFields(List<String> 要展示的字段集合) 示例："ID, NAME, PASSWORD"<br>
     *            sqlWhere 筛选组件map 示例：[{"action":"and","field":"qj_group","el":"like","model":"qjGroup","name":"任务组","type":"input","value":"1"}]
     * @return 返回map fields:字段列表 list:记录集合
     */
    SqlQueryWrapper.EUListBean queryDiyFormData(String modelId, Map<String, Object> map);

    /**
     * 查询自定义表单的对象数据
     * @param modelId　模型编号
     * @param id 主键编号
     * @return 返回表单对象
     */
    Object getFormData(String modelId,String id);

    /**
     * 根据模型和主键ids批量删除记录
     * @param modelId　模型编号
     * @param ids 表单主键编号集合
     */
    void deleteDiyFormData(String modelId,List<String> ids);

    /**
     * 拼接插入SQL<br>
     * 注意，map需要传换成小写
     * @param model 模型实体
     * @param params 参数map集合
     *               key: 为字段名
     *               value: 为字段值即存在数据库的值
     */
    void spliceInsertSql(ModelEntity model, Map<String, Object> params);

    /**
     * 拼接更新SQL<br>
     * 注意，map需要传换成小写
     * @param model 模型实体
     * @param params 参数map集合
     *               key: 为字段名
     *               value: 为字段值即存在数据库的值
     */
    void spliceUpdateSql(ModelEntity model, Map<String, Object> params);


    /**
     * 根据模型和关联编号查询模型数据
     * @param model　模型实体
     *               必须包含 modelTableName(表名)
     * @param linkId 模型关联编号，如文章编号
     * @return 返回模型数据
     */
    Map getModelDataByLinkId(ModelEntity model, String linkId);

}
