








package net.mingsoft.basic.biz.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import net.mingsoft.base.biz.impl.BaseBizImpl;
import net.mingsoft.base.dao.IBaseDao;
import net.mingsoft.base.entity.ResultData;
import net.mingsoft.basic.bean.RoleBean;
import net.mingsoft.basic.biz.IManagerBiz;
import net.mingsoft.basic.biz.IModelBiz;
import net.mingsoft.basic.biz.IRoleBiz;
import net.mingsoft.basic.biz.IRoleModelBiz;
import net.mingsoft.basic.dao.IModelDao;
import net.mingsoft.basic.dao.IRoleDao;
import net.mingsoft.basic.dao.IRoleModelDao;
import net.mingsoft.basic.entity.ManagerEntity;
import net.mingsoft.basic.entity.RoleEntity;
import net.mingsoft.basic.entity.RoleModelEntity;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


/**
 * 角色业务层接口实现类
 * @author 张敏
 * @version
 * 版本号：100-000-000<br/>
 * 创建日期：2012-03-15<br/>
 * 历史修订：<br/>
 */
@Service("roleBiz")
@Transactional
public class RoleBizImpl extends BaseBizImpl<IRoleDao, RoleEntity> implements IRoleBiz {

	/**
	 * 注入角色持久化层
	 */
	@Autowired
	private IRoleDao roleDao;

	@Autowired
	private IRoleModelBiz roleModelBiz;

	@Autowired
	private IModelBiz modelBiz;

	@Autowired
	private IManagerBiz managerBiz;

	/**
	 * 获取角色持久化层
	 * @return roleDao 返回角色持久化层
	 */
	@Override
	public IBaseDao getDao() {
		return roleDao;
	}


	@Override
	public boolean saveOrUpdateRole(RoleBean role) {

		//根据当前角色名称查询
		RoleBean oldRole = new RoleBean();
		oldRole.setRoleName(role.getRoleName());
		oldRole = (RoleBean) roleDao.getByEntity(oldRole);

		// 第一步：先判断是新增角色还是修改角色
		// 通过角色id判断是保存还是修改
		if(StringUtils.isNotEmpty(role.getId())){
			//若为更新角色，数据库中存在该角色名称且当前名称不为更改前的名称，则属于重名
			if(oldRole != null && !oldRole.getId().equals(role.getId())){
				return false;
			}
			roleDao.updateEntity(role);

		}else{
			//判断角色名是否重复
			if(oldRole != null){
				return false;
			}
			//获取管理员id
			this.save(role);
			roleDao.updateCache();
		}

		//第二步：更新角色对应的菜单
		//开始保存相应的关联数据。组织角色模块的列表。
		List<RoleModelEntity> roleModelList = new ArrayList<>();
		if(!StringUtils.isEmpty(role.getIds())){
			for(String id : role.getIds().split(",")){
				RoleModelEntity roleModel = new RoleModelEntity();
				roleModel.setRoleId(Integer.parseInt(role.getId()));
				roleModel.setModelId(Integer.parseInt(id));
				roleModelList.add(roleModel);
			}
			//先删除当前的角色关联菜单，然后重新添加。
			roleModelBiz.deleteEntity(Integer.parseInt(role.getId()));
			modelBiz.updateCache();

			//加上数量参数用于区分IBaseBiz的重名方法
			roleModelBiz.saveBatch(roleModelList, roleModelList.size());
		}else{
			roleModelBiz.deleteEntity(Integer.parseInt(role.getId()));
		}

		return true;
	}


	/**
	 * 根据角色集合，删除不包括当前管理员所属角色以及默认角色的所有角色，并删除关联角色模块
	 * @param roles 角色集合
	 * @param managerSession 当前管理员
	 * @return false:传参含有当前管理员所属角色或包含默认角色 ,true：删除成功
	 */
	@Override
	public boolean deleteRoleByRoles(List<RoleEntity> roles,ManagerEntity managerSession) {
		//提取roles中ID参数
		List<String> roleIdList = roles.stream().map(RoleEntity::getId).collect(Collectors.toList());
		int before = roleIdList.size();
		//提取managerSession中的角色信息
		List<String> managerRoleIds = Arrays.stream(managerSession.getRoleIds().split(",")).collect(Collectors.toList());
		//查询出所有默认角色
		LambdaQueryWrapper<RoleEntity> wrapper = new LambdaQueryWrapper<>();
		wrapper.eq(RoleEntity::getNotDel,1);
		List<RoleEntity> notDelRoles = super.list(wrapper);
		//默认角色不为空
		if (notDelRoles != null && notDelRoles.size()>0) {
			List<String> notDelRoleIds = notDelRoles.stream().map(RoleEntity::getId).collect(Collectors.toList());
			//筛选默认角色和当前管理员所属角色
			roleIdList.removeAll(notDelRoleIds);
		}
		roleIdList.removeAll(managerRoleIds);
		int after = roleIdList.size();
		//转为int数组
		int[] ids = roleIdList.stream().mapToInt(Integer::valueOf).toArray();
		//删除角色以及关联角色模块
		if (ids.length > 0){
			super.delete(ids);
			roleModelBiz.deleteByRoleIds(ids);
			super.updateCache();
			// 删除角色，刷新model缓存，否则菜单查询依然有数据
			modelBiz.updateCache();
		}
		//如果没有数据被筛选，则返回true
		if (before == after){
			return true;
		}
		return false;
	}
}
