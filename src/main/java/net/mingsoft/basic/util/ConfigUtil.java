



package net.mingsoft.basic.util;

import cn.hutool.core.map.MapUtil;
import cn.hutool.json.JSONUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * 自定义配置参数获取
 * 修订日期: 2022-1-27 新增getEntity()方法
 */
public class ConfigUtil {

    /*
     * log4j日志记录
     */
    protected static final Logger LOG = LoggerFactory.getLogger(ConfigUtil.class);


    /**
     * 配置缓存
     * key: 全局为global 否则为站点id
     * value: 缓存配置项  key: 配置名称  value: 配置
     */
    public static Map<String,Map<String,Map<String,Object>>> CONFIG_HASH_MAP = MapUtil.newHashMap();


    public static void init(Map<String, Map<String,Map<String,Object>>> siteConfigs){
        CONFIG_HASH_MAP.clear();
        CONFIG_HASH_MAP = siteConfigs;
    }


    /**
     * 返回字符串类型的数据
     * @param configName 配置名称  对应自定义配置列表上的 配置名称 字段
     * @param key 对应代码生成器中的字段名称 注意：名称是驼峰式
     * @return 无匹配返回空
     */
    public static String getString(String configName,String key) {
        Object object = getObject(configName, key);
        if(object==null){
            return "";
        }
        return object.toString();
    }

    /**
     * 返回字符串类型的数据
     * @param configName 配置名称  对应自定义配置列表上的 配置名称 字段
     * @param key 对应代码生成器中的字段名称 注意：名称是驼峰式
     * @param defaultValue 默认值,如果配置中没有值，会返回默认值
     * @return 无匹配返回默认值
     */
    public static String getString(String configName,String key, String defaultValue) {
        Object object = getObject(configName, key);
        if(object==null){
            return defaultValue;
        }
        return object.toString();
    }

    /**
     * 返回布尔类型的数据
     * @param configName 配置名称  对应自定义配置列表上的 配置名称 字段
     * @param key 对应代码生成器中的字段名称 注意：名称是驼峰式
     * @return 无匹配返回FALSE
     */
    public static boolean getBoolean(String configName,String key) {
        Object object = getObject(configName, key);
        if(object == null){
            return Boolean.FALSE;
        }
        if(object instanceof String) {
            return Boolean.parseBoolean(String.valueOf(object));
        } else {
            return (boolean)object;
        }
    }

    /**
     * 返回布尔类型的数据
     * @param configName 配置名称  对应自定义配置列表上的 配置名称 字段
     * @param key 对应代码生成器中的字段名称 注意：名称是驼峰式
     * @param defaultValue 默认值,如果配置中没有值，会返回默认值
     * @return 无匹配返回默认值
     */
    public static boolean getBoolean(String configName,String key, Boolean defaultValue) {
        Object object = getObject(configName, key);
        if(object == null){
            return defaultValue;
        }
        if(object instanceof String) {
            return Boolean.parseBoolean(String.valueOf(object));
        } else {
            return (boolean)object;
        }
    }



    /**
     * 返回整型类型的数据
     * @param configName 配置名称  对应自定义配置列表上的 配置名称 字段
     * @param key 对应代码生成器中的字段名称 注意：名称是驼峰式
     * @return 无匹配返回0
     */
    public static int getInt(String configName,String key) {
        Object object = getObject(configName, key);
        if(object==null){
            return 0;
        }
        if(object instanceof String) {
            return Integer.parseInt(String.valueOf(object));
        } else {
            return (int)object;
        }
    }

    /**
     * 返回整型类型的数据
     * @param configName 配置名称  对应自定义配置列表上的 配置名称 字段
     * @param key 对应代码生成器中的字段名称 注意：名称是驼峰式
     * @param defaultValue 默认值,如果配置中没有值，会返回默认值
     * @return 无匹配返回默认值
     */
    public static int getInt(String configName,String key,int defaultValue) {
        Object object = getObject(configName, key);
        if(object==null){
            return defaultValue;
        }
        if(object instanceof String) {
            return Integer.parseInt(String.valueOf(object));
        } else {
            return (int)object;
        }
    }

    /**
     * 返回loing类型的数据
     * @param configName 配置名称  对应自定义配置列表上的 配置名称 字段
     * @param key 对应代码生成器中的字段名称 注意：名称是驼峰式
     * @return 无匹配返回0
     */
    public static long getLong(String configName,String key) {
        Object object = getObject(configName, key);
        if(object==null){
            return 0;
        }
        if(object instanceof String) {
            return Long.parseLong(String.valueOf(object));
        } else {
            return (long)object;
        }
    }

    /**
     * 返回loing类型的数据
     * @param configName 配置名称  对应自定义配置列表上的 配置名称 字段
     * @param key 对应代码生成器中的字段名称 注意：名称是驼峰式
     * @param defaultValue 默认值,如果配置中没有值，会返回默认值
     * @return 无匹配返回默认值
     */
    public static long getLong(String configName,String key,int defaultValue) {
        Object object = getObject(configName, key);
        if(object==null){
            return defaultValue;
        }
        if(object instanceof String) {
            return Long.parseLong(String.valueOf(object));
        } else {
            return (long)object;
        }
    }


    /**
     * 如果不确定返回类型，可以使用 getObject
     * @param configName 配置名称  对应自定义配置列表上的 配置名称 字段
     * @param key 对应代码生成器中的字段名称 注意：名称是驼峰式
     * @return 无匹配返回null
     */
    public static Object getObject(String configName,String key) {
        Map<String, Object> entity = getEntityMap(configName);
        if (entity == null){
            return null;
        }
        if(entity.get("configData")==null){
            return null;
        }
        //将data转换成map
        HashMap map = JSONUtil.toBean(entity.get("configData").toString(), HashMap.class);
        if(map!=null){
            return map.get(key);
        }
        return null;
    }

    /**
     * 获取configName完整配置数据，通过一次性获取所有配置，避免重复传递 configName
     * @param configName 配置名称  对应自定义配置列表上的 配置名称 字段
     * @return map
     */
    public static Map getMap(String configName) {
        Map<String, Object> entity = getEntityMap(configName);
        if (entity == null) {
            return null;
        }
        return  JSONUtil.toBean(entity.get("configData").toString(), HashMap.class);
    }

    /**
     * 通过配置名称获取配置数据，默认优先从当前站获取配置，如果当前站没有配置，则获取全局配置
     * @param configName 配置名称
     * @return
     */
    public static Map<String,Object> getEntityMap(String configName){
        Map<String, Map<String, Object>> configMap = CONFIG_HASH_MAP.get("global");
        if (MapUtil.isEmpty(configMap)){
            return null;
        }
        if (BasicUtil.getWebsiteApp() == null){
            return configMap.get(configName);
        }
        Map<String, Map<String, Object>> siteConfigMap = CONFIG_HASH_MAP.get(BasicUtil.getWebsiteApp().getAppId());
        if (MapUtil.isEmpty(siteConfigMap)){
            return configMap.get(configName);
        }

        return siteConfigMap.get(configName) != null ? siteConfigMap.get(configName) : configMap.get(configName);
    }

    public static void saveOrUpdate(Map<String, Object> entity){
        String configName = entity.get("configName").toString();
        String appId = entity.get("appId").toString();
        Map<String, Map<String, Object>> map = CONFIG_HASH_MAP.get(appId);
        // 如果为空的话，创建一个，防止put出现空指针问题
        if (MapUtil.isEmpty(map)) {
            map = new HashMap<>();
        }
        map.put(configName, entity);
        CONFIG_HASH_MAP.put(appId, map);

    }

    /**
     * 移除指定站点的配置缓存 只有在自定义配置删除时才使用
     * @param configName 配置名称
     * @param appId 站点id
     */
    public static void removeEntity(String configName, String appId){
        Map<String, Map<String, Object>> configMap = CONFIG_HASH_MAP.get(appId);
        if (MapUtil.isEmpty(configMap)){
            return;
        }
        configMap.remove(configName);
    }

    /**
     * 移除全局配置缓存 只有在自定义配置删除时才使用
     * @param configName 配置名称
     */
    public static void removeEntity(String configName){
        removeEntity(configName,"global");
    }

}

