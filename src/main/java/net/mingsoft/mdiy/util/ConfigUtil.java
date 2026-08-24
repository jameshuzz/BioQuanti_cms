



package net.mingsoft.mdiy.util;

import cn.hutool.core.bean.BeanUtil;
import net.mingsoft.mdiy.entity.ConfigEntity;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

/**
 * 自定义配置参数获取
 * 修订日期: 2022-1-27 新增getEntity()方法
 * 2025-05-23 从basic ConfigUtil获取配置，该ConfigUtil额外提供获取ConfigEntity
 */
public class ConfigUtil extends net.mingsoft.basic.util.ConfigUtil{

    /**
     * 通过configName查询实体
     * @param configName 配置名称  对应自定义配置列表上的 配置名称 字段
     * @return ConfigEntity
     */
    public static ConfigEntity getEntity(String configName){
        if (StringUtils.isEmpty(configName) ) {
            return null;
        }

        Map<String, Object> configMap = net.mingsoft.basic.util.ConfigUtil.getEntityMap(configName);
        return BeanUtil.toBean(configMap, ConfigEntity.class);
    }

    public static void saveOrUpdate(Map configEntity){
        net.mingsoft.basic.util.ConfigUtil.saveOrUpdate(configEntity);
    }

    public static void saveOrUpdate(ConfigEntity configEntity){
        Map<String, Object> configMap = BeanUtil.beanToMap(configEntity);
        net.mingsoft.basic.util.ConfigUtil.saveOrUpdate(configMap);
    }

    /**
     * 移除全局配置 只有在自定义配置删除时才使用
     * @param configName 配置名称
     */
    public static void removeEntity(String configName){
        net.mingsoft.basic.util.ConfigUtil.removeEntity(configName);
    }

    /**
     * 移除指定站点缓存配置 只有在自定义配置删除时才使用
     * @param configName 配置名称
     * @param appId 站点id
     */
    public static void removeEntity(String configName, String appId){
        net.mingsoft.basic.util.ConfigUtil.removeEntity(configName, appId);
    }
}

