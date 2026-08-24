








package net.mingsoft.basic.dao;

import net.mingsoft.base.dao.IBaseDao;
import net.mingsoft.base.entity.BaseEntity;
import net.mingsoft.basic.entity.ManagerEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 管理员持久化
 * @author ms dev group
 * @version 
 * 版本号：100-000-000<br/>
 * 创建日期：2012-03-15<br/>
 * 历史修订：<br/>
 */
public interface IManagerDao extends IBaseDao<ManagerEntity> {

	
	/**
	 * 根据用户名修改用户密码
	 * @param manager 管理员实体
	 */
	public void updateUserPasswordByUserName(ManagerEntity manager);	
	
	
	/**
	 * 查询当前登录的管理员的所有子管理员
	 * @return 返回管理员集合
	 */
	public List<ManagerEntity> queryAllChildManager(int managerId);
	
}
