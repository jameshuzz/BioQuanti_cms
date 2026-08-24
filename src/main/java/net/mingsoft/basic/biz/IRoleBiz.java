








package net.mingsoft.basic.biz;

import net.mingsoft.base.biz.IBaseBiz;
import net.mingsoft.basic.bean.RoleBean;
import net.mingsoft.basic.entity.ManagerEntity;
import net.mingsoft.basic.entity.RoleEntity;

import java.util.List;


/**
 * @ClassName: BasicUtil
 * @Description: TODO(业务工具类)
 * @author 铭软开发团队
 * @date 2020年7月2日
 */
public interface IRoleBiz extends IBaseBiz<RoleEntity>{

    /**
     * 批量更新角色与权限关联数据
     * @param role 当前角色，通常由前端提交
     * @return false:一般是角色名称重复 ,true:更新成功
     */
    boolean saveOrUpdateRole(RoleBean role);

    /**
     * 根据角色集合，删除不包括当前管理员所属角色以及默认角色的所有角色，并删除关联角色模块
     * @param roles 角色集合
     * @param managerSession 当前管理员
     * @return false:传参含有当前管理员所属角色或包含默认角色 ,true：删除成功
     */
    boolean deleteRoleByRoles(List<RoleEntity> roles, ManagerEntity managerSession);
}
