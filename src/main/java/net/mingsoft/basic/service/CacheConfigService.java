
package net.mingsoft.basic.service;

import cn.hutool.core.map.CaseInsensitiveMap;
import cn.hutool.core.map.MapUtil;
import net.mingsoft.basic.util.ConfigUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CacheConfigService implements IBaseCacheConfigService{

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 从数据库中加载配置存入缓存
     */
    public void load() {
        String sql = "SELECT ID,MODEL_ID,CONFIG_NAME,CONFIG_DATA,CONFIG_TYPE,APP_ID FROM MDIY_CONFIG WHERE CONFIG_TYPE = ? AND DEL = 0";

        List<Map<String, Object>> configList = jdbcTemplate.queryForList(sql,"config");

        // 规范map的key
        List<Map<String, Object>> camelCaseConfigList = configList.stream()
                .map(configMap -> MapUtil.toCamelCaseMap(new CaseInsensitiveMap<>(configMap))) // 注意类似ID这样的全大写不含_的key 不会变化
                .collect(Collectors.toList());

        /**
         * 结构
         * appId:{
         *     configName: {
         *         configItem: configItemValue
         *     }
         * }
         */
        Map<String, Map<String, Map<String, Object>>> allConfig = new HashMap<>();
        camelCaseConfigList.forEach(config -> {
            String appId = config.get("appId").toString();
            String configName = config.get("configName").toString();

            allConfig.computeIfAbsent(appId, k -> new HashMap<>())
                    .computeIfAbsent(configName, k -> new HashMap<>())
                    .putAll(config);

        });
        // 配置初始化
        ConfigUtil.init(allConfig);
    }

}
