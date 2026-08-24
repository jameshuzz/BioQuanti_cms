








package net.mingsoft.basic.biz;

import net.mingsoft.base.biz.IBaseBiz;
import net.mingsoft.basic.entity.ModelEntity;
import net.mingsoft.basic.entity.RoleModelEntity;

import java.util.List;

/**
 * 角色模块关联业务层接口
 * @author 张敏
 * @version 
 * 版本号：100-000-000<br/>
 * 创建日期：2012-03-15<br/>
 * 历史修订：<br/>
 */
public interface IRoleModelBiz extends IBaseBiz<RoleModelEntity>{
	
	/**
	 * 保存该角色对应的模块集合
	 * @param roleModelList 集合
	 */
	void saveEntity(List<RoleModelEntity> roleModelList);
	
	/**
	 * 更新该角色对应的模块集合
	 * @param roleModelList 集合
	 */
	void updateEntity(List<RoleModelEntity> roleModelList);
	
	/**
	 * 通过角色获取所有关联的模块id
	 * @param roleId
	 */
	List<RoleModelEntity> queryByRoleId(int roleId);

	/**
	 * 根据角色id删除关联记录
	 * @param roleId
	 */
	void deleteByRoleId(int roleId);

	/**
	 * 根据角色id集合删除关联记录
	 * @param ids
	 */
	void deleteByRoleIds(int[] ids);

	/**
	 * 根据modelId删除关联roleId
	 * @param models id集合
	 */
	void deleteByModelIds(List<ModelEntity> models);

}
