








package net.mingsoft.basic.realm;

import net.mingsoft.basic.biz.IManagerBiz;
import net.mingsoft.basic.biz.IModelBiz;
import net.mingsoft.basic.constant.e.ManagerAdminEnum;
import net.mingsoft.basic.entity.ManagerEntity;
import net.mingsoft.basic.entity.ModelEntity;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * 管理员shiro权限控制
 *
 * @author 铭软团队
 * @version 版本号：<br/>
 *          创建日期：2015年9月9日<br/>
 *          历史修订：<br/>
 */
public class ManagerAuthRealm extends BaseAuthRealm {
	/**
	 * 管理员业务层
	 */
	@Autowired
	private IManagerBiz managerBiz;

	/**
	 * 模块业务层
	 */
	@Autowired
	private IModelBiz modelBiz;

	public ManagerAuthRealm() {
		super.setName(this.getClass().getName());
	}

	/**
	 * 新登用户验证
	 */
	protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
		UsernamePasswordToken upToken = (UsernamePasswordToken) token;
		ManagerEntity newManager = new ManagerEntity();
		newManager.setManagerName(upToken.getUsername());
		ManagerEntity manager = (ManagerEntity) managerBiz.getEntity(newManager);
		if (manager != null) {
			return new SimpleAuthenticationInfo(manager, manager.getManagerPassword(), getName());
		}
		return null;
	}

	/**
	 * 功能操作授权
	 */
	protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
		ManagerEntity manager = null;
		if (principalCollection.getPrimaryPrincipal() instanceof ManagerEntity _manager) {
			manager = (ManagerEntity) managerBiz.getEntity(_manager);
		}

		if (null == manager) {
			return null;
		}
		List<ModelEntity> models;
		SimpleAuthorizationInfo result = new SimpleAuthorizationInfo();
		// 授权当前角色管理员标识，防止会员请求到管理员接口
		result.addRole(CustomUserNamePasswordToken.AuthType.MANAGER.toString());
		if(ManagerAdminEnum.SUPER.toString().equals(manager.getManagerAdmin())){
			// 超管 super标识直接获取所有权限
			models = modelBiz.list();
		} else {
			// 查询管理员对应的角色
			models = modelBiz.queryModelByRoleId(manager.getRoleId());
		}
		for (ModelEntity e:models) {
			ModelEntity me =  e;
			if (!StringUtils.isEmpty(me.getModelUrl())) {
				result.addStringPermission(me.getModelUrl());
			}
		}
		return result;
	}

	/**
	 * 多realm下支持查询当前realm类是否是正确的角色授权类
	 * @param token the token being submitted for authentication.
	 * @return
	 */
	@Override
	public boolean supports(AuthenticationToken token) {
		if (token instanceof CustomUserNamePasswordToken customUserNamePasswordToken) {
			return customUserNamePasswordToken.getAuthType() == CustomUserNamePasswordToken.AuthType.MANAGER;
		}
		return false;
	}

	/**
	 * 检查当前登陆人是否有权限
	 * @param principals the application-specific subject/user identifier.
	 * @param permission the String representation of a Permission that is being checked.
	 * @return
	 */
	@Override
	public boolean isPermitted(PrincipalCollection principals, String permission){
		// 防止登录后请求接口权限错乱
		if (!isManagerPrincipals(principals)) {
			return false;
		}
		ManagerEntity manager = (ManagerEntity)principals.getPrimaryPrincipal();
		return ManagerAdminEnum.SUPER.toString().equalsIgnoreCase(manager.getManagerAdmin())||super.isPermitted(principals,permission);
	}

	/**
	 * 检查当前登录人是否有角色
	 * @param principals the application-specific subject/user identifier.
	 * @param roleIdentifier   the application-specific role identifier (usually a role id or role name).
	 * @return
	 */
	@Override
	public boolean hasRole(PrincipalCollection principals, String roleIdentifier) {
		if (isManagerPrincipals(principals) && CustomUserNamePasswordToken.AuthType.MANAGER.toString().equals(roleIdentifier)) {
			ManagerEntity manager = (ManagerEntity)principals.getPrimaryPrincipal();
			return ManagerAdminEnum.SUPER.toString().equalsIgnoreCase(manager.getManagerAdmin()) || super.hasRole(principals,roleIdentifier);
		}
		return false;
	}

	/**
	 * 判断当前登陆人是否时ManagerEntity实体
	 * @param principals 当前登录人的信息集合
	 * @return true or false
	 */
	private boolean isManagerPrincipals(PrincipalCollection principals) {
		return principals.getPrimaryPrincipal() instanceof ManagerEntity;
	}

}
