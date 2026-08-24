
package net.mingsoft.mdiy.aop;

import net.mingsoft.base.entity.ResultData;
import net.mingsoft.basic.aop.BaseAop;
import net.mingsoft.basic.entity.AppEntity;
import net.mingsoft.basic.util.BasicUtil;
import net.mingsoft.mdiy.biz.IConfigBiz;
import net.mingsoft.mdiy.constant.e.ConfigTypeEnum;
import net.mingsoft.mdiy.entity.ConfigEntity;
import net.mingsoft.mdiy.util.ConfigUtil;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 配置自定义标签,在保存自定义配置和全局标签时，更新appId
 * @author 铭软开发团队
 * @ClassName:
 * @Description:
 * @date 2025-01-06
 */
@Component("configAop")
@Aspect
public class ConfigAop extends BaseAop {

    @Autowired
    private IConfigBiz configBiz;

    @Pointcut("execution(* net.mingsoft.mdiy.action.ConfigAction.importJson(..)) || execution(* net.mingsoft.mdiy.action.GlobalTagAction.importJson(..))")
    public void save() {
    }


    /**
     * 更新自定义配置 同步更新configUtil缓存中的数据
     */
    @Pointcut("execution(* net.mingsoft.mdiy.action.ConfigDataAction.update(..))")
    public void update() {
    }

    /**
     * 切自定义配置和全局自定义标签时导入，更新appId
     * @param jp
     * @param result
     */
    @AfterReturning(value = "save()", returning = "result")
    public void save(JoinPoint jp, Object result) {
        ResultData resultData = (ResultData) result;
        if (!resultData.isSuccess()) {
            return;
        }
        AppEntity app = BasicUtil.getWebsiteApp();
        ConfigEntity configEntity = resultData.getData(ConfigEntity.class);
        // 由于这里处理站群补充appId的，判断当前环境是站群环境下
        if (app == null) {
            if (ConfigTypeEnum.CONFIG.getType().equals(configEntity.getConfigType())){
                ConfigUtil.saveOrUpdate(configEntity);
            }
            return;
        }

        configEntity.setAppId(app.getAppId());
        configBiz.updateById(configEntity);
        // 刷新数据查询缓存
        configBiz.updateCache();
        if (ConfigTypeEnum.CONFIG.getType().equals(configEntity.getConfigType())){
            ConfigUtil.saveOrUpdate(configEntity);
        }
    }

    @AfterReturning(value = "update()", returning = "result")
    public void update(JoinPoint jp, Object result) {
        ResultData resultData = (ResultData) result;
        if (!resultData.isSuccess()) {
            return;
        }
        ConfigEntity configEntity = resultData.getData(ConfigEntity.class);
        ConfigUtil.saveOrUpdate(configEntity);
    }

}
