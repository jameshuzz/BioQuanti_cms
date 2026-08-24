








package net.mingsoft.basic.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import net.mingsoft.base.entity.BaseEntity;
import org.apache.commons.lang3.StringUtils;

/**
 * 管理员实体类
 *
 * @author ms dev group
 * @version 版本号：100-000-000<br/>
 * 创建日期：2012-03-15<br/>
 * 历史修订：增加逻辑伪删<br/>
 * 2022-1-5 将角色改为多角色 roleIds<br/>
 */
@TableName("manager")
public class ManagerEntity extends BaseEntity {



    /**
     * 帐号
     */
    private String managerName;

    /**
     * 昵称
     */
    @TableField("MANAGER_NICKNAME")
    private String managerNickName;

    /**
     * 角色名
     * 在查询此时登录的管理员的子管理员列表开始时用的到
     */
    @TableField(exist = false)
    private String roleName;

    /**
     * 密码
     */
    @TableField(updateStrategy = FieldStrategy.NOT_EMPTY)
    private String managerPassword;

    /**
     * 目前只在业务系统中使用
     */
    private String managerAdmin = "";

    /**
     * 角色ID集合
     */
    private String roleIds;

    /**
     * 开启逻辑伪删
     */
    private Integer del;



    /**
     * 获取角色名
     *
     * @return
     */
    public String getRoleName() {
        return roleName;
    }

    public String getManagerAdmin() {
        return managerAdmin;
    }

    public void setManagerAdmin(String managerAdmin) {
        this.managerAdmin = managerAdmin;
    }

    /**
     * 设置角色名
     *
     * @param roleName
     */
    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }


    /**
     * 获取managerName
     *
     * @return managerName
     */
    public String getManagerName() {
        return managerName;
    }

    /**
     * 设置managerName
     *
     * @param managerName
     */
    public void setManagerName(String managerName) {
        this.managerName = managerName;
    }

    /**
     * 获取managerPassword
     *
     * @return managerPassword
     */
    public String getManagerPassword() {
        return managerPassword;
    }

    /**
     * 设置managerPassword
     *
     * @param managerPassword
     */
    public void setManagerPassword(String managerPassword) {

        this.managerPassword = managerPassword;
    }

    /**
     * 获取managerNickName
     *
     * @return managerNickName
     */
    public String getManagerNickName() {
        return managerNickName;
    }

    /**
     * 设置managerNickName
     *
     * @param managerNickName
     */
    public void setManagerNickName(String managerNickName) {
        this.managerNickName = managerNickName;
    }

    public String getRoleIds() {
        return roleIds;
    }

    public void setRoleIds(String roleIds) {
        this.roleIds = roleIds;
    }

    /**
     * 开源版本使用
     * @return
     */
    public int getRoleId() {
        if(StringUtils.isNotBlank(roleIds)) {
            return Integer.parseInt(roleIds.split(",")[0]);
        }
        return 0;
    }
    /**
     * 开源版本使用
     */
    public void setRoleId(int roleId) {
        this.roleIds = String.valueOf(roleId);
    }

    /**
     * 添加站群容错处理
     *
     * @param roleName
     */
    public void setRoleNames(String roleName) {
        this.roleName = roleName;
    }

    /**
     * 添加站群容错处理
     *
     * @return
     */
    public String getRoleNames() {
        return roleName;
    }
}
