
package net.mingsoft.mdiy.aop;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.map.CaseInsensitiveMap;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import net.mingsoft.base.entity.ResultData;
import net.mingsoft.base.exception.BusinessException;
import net.mingsoft.basic.aop.BaseAop;
import net.mingsoft.basic.util.BasicUtil;
import net.mingsoft.basic.util.SpringUtil;
import net.mingsoft.mdiy.biz.IModelBiz;
import net.mingsoft.mdiy.entity.ModelEntity;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 自定义模型、业务数据处理切面
 * 目前使用场景
 *      保存、编辑数据时，检测不可重复字段是否满足
 *
 */

@Component
@Aspect

public class ModelDataAop extends BaseAop {

    @Autowired
    private IModelBiz modelBiz;


    @Pointcut("execution(* net.mingsoft.mdiy.action.ModelAction.save(..)) || execution(* net.mingsoft.mdiy.action.ModelAction.update(..))")
    public void modelDataCheck() {
    }

    @Pointcut(value = "execution(* net.mingsoft.mdiy.biz.impl.ModelDataImpl.saveDiyFormData(..)) || execution(* net.mingsoft.mdiy.biz.impl.ModelDataImpl.updateDiyFormData(..))")
    public void formDataCheck(){
    }

    /**
     * 检查自定义业务数据重复
     * @param jp
     */
    @Before(value = "formDataCheck()")
    public void formDataCheck(JoinPoint jp) {
        LOG.debug("modelDataAop");
        ModelEntity model = getType(jp, ModelEntity.class);
        if (model==null){
            // 对modelId参数方法做处理
            String modelId = BasicUtil.getString("modelId", "");
            model = modelBiz.getById(modelId);
        }
        Map<String,Object> params = getType(jp, CaseInsensitiveMap.class);
        if (model!=null){
            if (!this.checkDataRepeat(model, params)) {
                throw new BusinessException("存在不可重复的字段值，请检查输入数据是否有重复。");
            }
        }
    }

    /**
     * 检查自定义模型数据是否重复
     * @param pjp
     */
    @Around(value = "modelDataCheck()")
    public ResultData modelDataCheck(ProceedingJoinPoint pjp) throws Throwable {
        LOG.debug("开始检测自定义模型是否重复");
        String modelId = BasicUtil.getString("modelId", "");
        if (StringUtils.isBlank(modelId)) {
            return ResultData.build().error();
        }
        ModelEntity model = modelBiz.getById(modelId);
        if (model!=null){
            Map<String, Object> params = BasicUtil.assemblyRequestMap();
            if (!this.checkDataRepeat(model, params)) {
                return ResultData.build().error("存在不可重复的字段值，请检查输入数据是否有重复。");
            }
        }
        Object result = pjp.proceed(pjp.getArgs());
        return JSONUtil.toBean(JSONUtil.toJsonStr(result), ResultData.class);
    }

    /**
     * 检查相关字段不可重复，如有有一个字段重复，则不通过提示
     * @param model 模型实体
     * @param params 提交参数
     */
    private boolean checkDataRepeat(ModelEntity model, Map<String,Object> params) {
        JdbcTemplate jdbcTemplate = SpringUtil.getBean(JdbcTemplate.class);

        // 获取当前提交的id
        Object id = params.get("id");

        // 不可重复字段是否满足
        Map repeatMap = new CaseInsensitiveMap(model.getRepeatMap());

        //设置区分大小写的Map,因为代码生成器存在版本问题，有些老数据是大小写没统一
        Map fieldMap =  new CaseInsensitiveMap<>(model.getFieldMap());
        // 组织不可重复查询的where条件部分
        StringBuilder where = new StringBuilder();
        List values = new ArrayList();
        for (String paramKey  : params.keySet()) {
            //判断是否存在此字段
            if (fieldMap.containsKey(paramKey)) {

                // 此字段值是否可重复
                boolean repeat = MapUtil.getBool(repeatMap, paramKey, false);

                if (repeat){
                    // 不可重复 进行查询校验
                    if (StringUtils.isNotBlank(where)){
                        where.append(" OR ");
                    }
                    where.append(fieldMap.get(paramKey).toString()).append(" = ? ");
                    values.add(params.get(paramKey));
                }
            }
        }
        if (CollectionUtil.isNotEmpty(values)){
            String select = "select * from {} where ({})";

            // 编辑情况
            if (ObjectUtil.isNotEmpty(id)){
                // 如果有id，则加上排除当前id
                select += " AND id != ? ";
                values.add(id);
            }

            String  _select = StrUtil.format(select,model.getModelTableName(),where);
            LOG.debug("预执行SQL:{},params:{}",_select,Arrays.toString(values.toArray()));

            // 调用底层，避免sql字段值类型问题
            List<Map<String, Object>> results = jdbcTemplate.queryForList(_select, values.toArray());
            if (CollectionUtil.isNotEmpty(results)){
                return false;
            }
        }
        return true;
    }

}
