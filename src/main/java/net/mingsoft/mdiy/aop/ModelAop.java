
package net.mingsoft.mdiy.aop;

import net.mingsoft.basic.aop.BaseAop;
import net.mingsoft.basic.util.BasicUtil;
import net.mingsoft.mdiy.bean.ModelJsonBean;
import net.mingsoft.mdiy.constant.e.ModelCustomTypeEnum;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

/**
 * 自定义模型、业务导入切面处理
 * 目前使用场景
 *      站群情况下，避免导入模型、业务表名相同，在表名后面拼接appId区分
 *
 */

@Component
@Aspect

public class ModelAop extends BaseAop {


    /**
     * 站群情况下 给模型表名拼接appId,模型名称不做处理 自定义业务前台通过名称获取模型,会拼appId区分站点
     */
    @Before(value = "execution(* net.mingsoft.mdiy.biz.impl.ModelBizImpl.importConfig(..)) || execution(* net.mingsoft.mdiy.biz.impl.ModelBizImpl.importModel(..)) || execution(* net.mingsoft.mdiy.biz.impl.ModelBizImpl.updateConfig(..))")
    public void updateModelName(JoinPoint jp){
        if (BasicUtil.getWebsiteApp() != null){
            Object[] args = jp.getArgs();
            if (!ModelCustomTypeEnum.CONFIG.getLabel().equals(args[0])){
                // 自定义类型为模型或业务 则对表名拼接appId
                String appId = BasicUtil.getWebsiteApp().getAppId();
                ModelJsonBean modelJsonBean = (ModelJsonBean)args[1];
                String tableName = modelJsonBean.getTableName();
                modelJsonBean.setTableName(tableName+"_"+appId);
                if(modelJsonBean.getSql() != null ) {
                    String sql = modelJsonBean.getSql();
                    sql = sql.replaceAll("\\{model}"+tableName,"{model}"+modelJsonBean.getTableName());
                    modelJsonBean.setSql(sql);
                }

            }
        }
    }


}
