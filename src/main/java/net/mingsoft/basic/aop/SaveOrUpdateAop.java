







package net.mingsoft.basic.aop;

import cn.hutool.core.date.DateUtil;
import net.mingsoft.base.entity.BaseEntity;
import net.mingsoft.basic.entity.ManagerEntity;
import net.mingsoft.basic.util.BasicUtil;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 拦截保存更新方法，设置操作人和时间
 * @author by Administrator
 * @Description TODO
 * @date 2019/11/12 10:14
 */
@Component
@Aspect
public class SaveOrUpdateAop extends BaseAop {

    @Pointcut("execution(* net.mingsoft..*Action.save(..)) && !execution(* net.mingsoft..people..*Action.save(..))")
    public void save() {

    }

    @Pointcut("execution(* net.mingsoft..*Action.update(..)) && !execution(* net.mingsoft..people..*Action.update(..))")
    public void update() {
    }

    @Before("save()")
    public void save(JoinPoint jp) {
        Date date = new Date();
        List<BaseEntity> baseEntities = extractBaseEntity(jp);
        ManagerEntity manager = BasicUtil.getManager();
        String managerId = manager != null ? manager.getId() : "";
        baseEntities.forEach(baseEntity -> {
            baseEntity.setCreateDate(DateUtil.beginOfSecond(date));
            baseEntity.setUpdateDate(DateUtil.beginOfSecond(date));
            baseEntity.setCreateBy(managerId);
        });
    }


    @Before("update()")
    public void update(JoinPoint jp) {
        // 截断毫秒值，避免毫秒值大于500时，数据库级别秒值加1
        List<BaseEntity> baseEntities = extractBaseEntity(jp);
        ManagerEntity manager = BasicUtil.getManager();
        String managerId = manager != null ? manager.getId() : "";
        baseEntities.forEach(baseEntity -> {
            baseEntity.setUpdateDate(DateUtil.beginOfSecond(new Date()));
            baseEntity.setUpdateBy(managerId);
        });
    }


    /**
     * 从jp中获取参数 支持单对象和json数组参数
     * @param jp 切入点
     * @return List<BaseEntity>
     */
    private List<BaseEntity> extractBaseEntity(JoinPoint jp) {
        List<BaseEntity> entities = new ArrayList<>();

        // 1. 检查直接传递的BaseEntity参数
        BaseEntity directEntity = getType(jp, BaseEntity.class, true);
        if (directEntity != null) {
            entities.add(directEntity);
            return entities; // 直接参数优先处理
        }

        // 2. 检查JSON参数（List<? extend BaseEntity>）
        Object jsonParam = getJsonParam(jp);
        if (jsonParam instanceof List<?> list) {
            list.forEach(item->{
                // 非baseEntity对象不处理
                if (item instanceof BaseEntity baseEntity){
                    entities.add(baseEntity);
                }
            });
        }

        return entities;
    }

}
