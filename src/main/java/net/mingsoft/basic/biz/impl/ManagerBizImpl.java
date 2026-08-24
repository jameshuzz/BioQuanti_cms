









package net.mingsoft.basic.biz.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import net.mingsoft.base.biz.impl.BaseBizImpl;
import net.mingsoft.base.dao.IBaseDao;
import net.mingsoft.basic.biz.IManagerBiz;
import net.mingsoft.basic.dao.IManagerDao;
import net.mingsoft.basic.dao.IRoleDao;
import net.mingsoft.basic.entity.ManagerEntity;
import net.mingsoft.basic.entity.RoleEntity;
import net.mingsoft.basic.util.SpringUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


/**
 *
 *
 * <p>
 * <b>后台管理系统</b>
 * </p>
 *
 * <p>
 * Copyright: Copyright (c) 2014 - 2015
 * </p>
 *
 * <p>
 * Company:BioQuanti
 * </p>
 *
 * @version 300-001-001
 *
 * <p>
 * Comments:管理员业务层实现类，继承BaseBizImpl，实现IManagerBiz
 * </p>
 *
 * <p>
 * Create Date:2014-7-14
 * </p>
 *
 * <p>
 * Modification history:
 * </p>
 */
@Service("managerBiz")
@Transactional
public class ManagerBizImpl extends BaseBizImpl<IManagerDao, ManagerEntity> implements IManagerBiz {

	/**
	 * 注入管理员持久化层
	 */
    private IManagerDao managerDao;

    /**
	 * 获取管理员持久化层
	 * @return managerDao 返回管理员持久化层
	 */
    public IManagerDao getManagerDao() {
        return managerDao;
    }

    /**
	 * 设置managerDao
	 * @param managerDao 管理员持久化层
	 */
    @Autowired
    public void setManagerDao(IManagerDao managerDao) {
    	
       this.managerDao = managerDao;
    }

	/**
	 * 获取管理员持久化层
	 * @return managerDao 返回管理员持久化层
	 */
    @Override
    public IBaseDao getDao() {
    	
       return managerDao;
    }

	@Override
	public void updateUserPasswordByUserName(ManagerEntity manager) {
		
		managerDao.updateUserPasswordByUserName(manager);
	}

	@Override
	public List<ManagerEntity> queryAllChildManager(int managerId){
    	return managerDao.queryAllChildManager(managerId);
	}

	@Override
	public List<ManagerEntity> queryAllManager(List<ManagerEntity> managerList) {
		// 查询所有的角色
		IRoleDao roleDao = SpringUtil.getBean(IRoleDao.class);
		List<RoleEntity> roleList = roleDao.selectList(new QueryWrapper<>());
		// 获取所有管理员
		for (ManagerEntity manager : managerList) {
			StringBuilder roleNames = new StringBuilder();
			String[] roleIds = manager.getRoleIds().split(",");
			// 将当前管理员的角色名称用逗号拼接
			List<String> allRoleName = new ArrayList<>();
			for (String roleId : roleIds) {
				List<String> roleNameList = roleList.stream().filter(roleEntity -> roleEntity.getId().equals(roleId)).map(RoleEntity::getRoleName).collect(Collectors.toList());
				allRoleName.addAll(roleNameList);
			}
			roleNames.append(StringUtils.join(allRoleName, ","));
			manager.setRoleName(roleNames.toString());
		}
		return managerList;
	}

	/**
	 * 根据账号名查询
	 * @param managerName 传入的账号
	 * @return 返回结果
	 */
	@Override
	public ManagerEntity getManagerByManagerName(String managerName) {
    	ManagerEntity managerEntity = new ManagerEntity();
    	managerEntity.setManagerName(managerName);
		return (ManagerEntity) managerDao.getByEntity(managerEntity);
	}


}
