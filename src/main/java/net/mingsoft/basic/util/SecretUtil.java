
package net.mingsoft.basic.util;

import cn.hutool.json.JSONUtil;
import net.mingsoft.basic.entity.ManagerEntity;
import net.mingsoft.basic.realm.CustomUserNamePasswordToken;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Map;

/**
 * 密级条件添加工具类
 */
public class SecretUtil {


    protected static final Logger LOG = LoggerFactory.getLogger(SecretUtil.class);

    /**
     * 密级值
     */
    public static final String SECRET = "secret";

    /**
     * 管理员id
     */
    public static final String MANAGER_ID = "managerId";

    /**
     * 会员id
     */
    public static final String PEOPLE_ID = "peopleId";


    /**
     * 添加密级参数，管理员和会员互斥 不会同时登录
     */
    public static void addSecretParam(Map param){
        if (!isEnableSecret()){
            return;
        }

        param.put(SECRET, "");
        param.put(MANAGER_ID, "");
        param.put(PEOPLE_ID, "");

        Subject subject = SecurityUtils.getSubject();
        if (subject == null || !subject.isAuthenticated()) {
            return;
        }

        ManagerEntity manager = BasicUtil.getManager();
        JdbcTemplate jdbcTemplate = SpringUtil.getBean(JdbcTemplate.class);
        if (manager != null){
            // 获取当前登录管理员的密级值
            String managerSecret = jdbcTemplate.queryForObject("SELECT _SECRET FROM MANAGER WHERE ID = ?", String.class,manager.getId());
            param.put(SECRET, managerSecret);
            param.put(MANAGER_ID, manager.getId());
        } else if (subject.hasRole(CustomUserNamePasswordToken.AuthType.PEOPLE.name())) { // 处理会员密级 判断是否为 people 角色
            try {
                Map<String, Object> peopleMap = JSONUtil.toBean(JSONUtil.toJsonStr(subject.getPrincipal()), Map.class);
                String peopleId = peopleMap.get("id").toString();
                if (StringUtils.isNotBlank(peopleId)) {
                    String peopleSecret = jdbcTemplate.queryForObject("SELECT _SECRET FROM PEOPLE WHERE ID = ?", String.class, peopleId);
                    param.put(SECRET, peopleSecret);
                    param.put(PEOPLE_ID, getPeopleCreateBy(peopleId));
                }
            } catch (Exception e) {
                LOG.error("解析 people principal 失败", e);
            }
        }

    }

    /**
     * 是否启用密级
     */
    public static boolean isEnableSecret(){
        return ConfigUtil.getBoolean("文章分保配置","secretEnable",false);
    }


    /**
     * 获取会员创建人
     * @param id 会员id
     */
    public static String getPeopleCreateBy(String id) {
        return "u_"+id;
    }

}
