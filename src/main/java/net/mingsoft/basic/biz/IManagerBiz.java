









package net.mingsoft.basic.biz;

import net.mingsoft.base.biz.IBaseBiz;
import net.mingsoft.basic.entity.ManagerEntity;

import java.util.List;


/**
 * 管理员业务层，接口，继承IBaseBiz
 * @author 张敏
 * @version
 * 版本号：100-000-000<br/>
 * 创建日期：2012-03-15<br/>
 * 历史修订：2022-1-12 管理员更改为多角色<br/>
 * 2022-1-12 增加queryAllManager方法返回多角色管理员
 * 2022-1-27 把co的IManagerBiz移动到basic
 */
public interface IManagerBiz extends IBaseBiz<ManagerEntity> {


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

	/**
	 * 将角色IDS转换为多角色名
	 * @return 带有多角色名的管理员列表
	 */
	public List<ManagerEntity> queryAllManager(List<ManagerEntity> managerList);

	/**
	 * 根据账号名查询
	 * @param managerName 传入的账号
	 * @return 返回结果
	 */
	public ManagerEntity getManagerByManagerName(String managerName);
}
