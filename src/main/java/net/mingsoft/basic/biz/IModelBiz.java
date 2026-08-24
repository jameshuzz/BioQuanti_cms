








package net.mingsoft.basic.biz;

import net.mingsoft.base.biz.IBaseBiz;
import net.mingsoft.base.constant.e.BaseEnum;
import net.mingsoft.basic.entity.ModelEntity;
import net.mingsoft.basic.entity.RoleModelEntity;

import java.util.List;

/**
 * 模块业务接口
 * @author ms dev group
 * @version
 * 版本号：100-000-000<br/>
 * 创建日期：2012-03-15<br/>
 * 历史修订：<br/>
 */
public interface IModelBiz extends IBaseBiz<ModelEntity> {


	/**
	 * 根据角色ID查询模块集合
	 * @param roleId 角色ID
	 * @return 返回模块集合
	 */
	List<ModelEntity> queryModelByRoleId(int roleId);

	/**
	 * 根据模块编号查询模块实体
	 * @param modelCode 模块编号
	 * @return 返回模块实体
	 */
	ModelEntity getEntityByModelCode(BaseEnum modelCode);

	/**
	 * 根据模块编号查询模块实体
	 * @param modelCode 模块编号
	 * @return 返回模块实体
	 */
	ModelEntity getEntityByModelCode(String modelCode);

	/**
	 * 根据模块id获取当前项目中的分类模块id，规则根据modelcode定。**99******,只用是第３位与第４位９９
	 * @param modelType 模块类型　
	 * @param modelId 模块ID
	 * @return 返回模块实体
	 */
	ModelEntity getModel(String modelType,int modelId);

	/**	菜单新增递归方法
	 * @deprecated 此方法已废弃 推荐使用importModel方法
	 * @param modelParent 菜单实体
	 * @param parentIds 父级ids
	 * @param mangerRoleId  管理员id
	 * @param roleModels 角色mode数组
	 * @param parentId 父级id
	 */
    void reModel(ModelEntity modelParent, String parentIds, int mangerRoleId, List<RoleModelEntity> roleModels, Integer parentId);

	/**
	 * 字符串转菜单
	 * @deprecated 此方法已废弃 推荐使用importModel方法
	 * @param menuStr 菜单json
	 * @param mangerRoleId 当前管理员的角色id
	 * @param modelId 父级菜单编号，用于导入子菜单设置父级菜单编号
	 */
	void jsonToModel(String menuStr,int mangerRoleId,int modelId);

	/**
	 * 导入菜单
	 * @param modelEntity 菜单实体
	 * @param mangerRoleId 当前管理员的角色id
	 * @param parentIds 父级菜单编号集
	 * @param modelId 父级菜单编号，用于导入子菜单设置父级菜单编号
	 */
	void importModel(ModelEntity modelEntity, int mangerRoleId, String parentIds, Integer modelId);

	/**
	 * 修改
	 * @param model
	 */
	void updateEntity(ModelEntity model);

	/**
	 * 保存
	 * @param model
	 */
	void saveEntity(ModelEntity model);

	/**
	 * 此方法不动，怕影响其他地方使用
	 * 查询当前菜单下的一级菜单
	 * @param modelEntity
	 * @return
	 */
	List<ModelEntity> queryChildList(ModelEntity modelEntity);

	/**
	 * 查询当前菜单所有的子集菜单和权限集合
	 * @param modelEntity
	 * @return 菜单集合
	 */
	List<ModelEntity> queryAllChildList(ModelEntity modelEntity);

	/**
	 * 保存菜单，并且为角色添加该新增菜单的权限（super不会给绑定的角色添加新增菜单的权限）
	 *  @param modelEntity
	 */
    void saveModel(ModelEntity modelEntity);

	/**
	 * 根据菜单编号删除菜单
	 * @param modelEntityList 菜单集合
	 */
	void delete(List<ModelEntity> modelEntityList);
}
