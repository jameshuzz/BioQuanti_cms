








package net.mingsoft.basic.biz.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import net.mingsoft.base.biz.impl.BaseBizImpl;
import net.mingsoft.base.constant.e.BaseEnum;
import net.mingsoft.base.constant.e.DeleteEnum;
import net.mingsoft.base.dao.IBaseDao;
import net.mingsoft.basic.biz.IModelBiz;
import net.mingsoft.basic.biz.IRoleModelBiz;
import net.mingsoft.basic.constant.e.ManagerAdminEnum;
import net.mingsoft.basic.constant.e.ModelIsMenuEnum;
import net.mingsoft.basic.dao.IModelDao;
import net.mingsoft.basic.entity.ManagerEntity;
import net.mingsoft.basic.entity.ModelEntity;
import net.mingsoft.basic.entity.RoleModelEntity;
import net.mingsoft.basic.exception.BusinessException;
import net.mingsoft.basic.util.BasicUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 模块业务接口实现类
 * @author 张敏
 * @version
 * 版本号：100-000-000<br/>
 * 创建日期：2012-03-15<br/>
 * 历史修订：<br/>
 */
@Service("modelBiz")
@Transactional
public class ModelBizImpl extends BaseBizImpl<IModelDao, ModelEntity> implements IModelBiz{



	@Override
	public ModelEntity getEntityByModelCode(BaseEnum modelCode){
		
		return modelDao.getEntityByModelCode(modelCode.toString());
	}

	@Override
	public ModelEntity getEntityByModelCode(String modelCode) {
		
		return modelDao.getEntityByModelCode(modelCode);
	}

	/**
	 * 模块持久化层
	 */
    private IModelDao modelDao;
	@Autowired
    private IRoleModelBiz roleModelBiz;


	/**
	 * 获取模块持久化层
	 * @return modelDao 返回模块持久化层
	 */
    public IModelDao getModelDao() {
        return modelDao;
    }

    @Autowired
    public void setModelDao(IModelDao modelDao) {
        this.modelDao = modelDao;
    }

    @Override
    protected IBaseDao getDao() {
        
        return modelDao;
    }

	@Override
	public ModelEntity getModel(String modelType,int modelId) {
		
		return modelDao.getModel(modelType,modelId);
	}

	@Override
	public List<ModelEntity> queryModelByRoleId(int roleId) {
		return modelDao.queryModelByRoleId(roleId);
	}

	@Deprecated
	@Override
	@Transactional(rollbackFor = {Exception.class, Error.class})
	public void reModel(ModelEntity modelParent, String parentIds, int mangerRoleId, List<RoleModelEntity> roleModels, Integer parentId){
		//判断是否有子集，有则是菜单没有则是功能
		modelParent.setModelIsMenu(ObjectUtil.isNotNull(modelParent.getModelChildList())&&modelParent.getModelChildList().size()>0
				? Integer.valueOf(1):Integer.valueOf(0));
		//设置父级id,null不会存进数据库
		modelParent.setModelId(parentId);
		modelParent.setModelDatetime(new Timestamp(System.currentTimeMillis()));
		modelParent.setModelParentIds(parentIds);
		ModelEntity modelParentEntity = getEntityByModelCode(modelParent.getModelCode());
		if (modelParentEntity == null) {
			//判断菜单名称是否已存在
			if(modelParent.getModelIsMenu() == ModelIsMenuEnum.MODEL_MEUN.toInt()){
				ModelEntity modelEntity = new ModelEntity();
				modelEntity.setModelIsMenu(ModelIsMenuEnum.MODEL_MEUN.toInt());
				modelEntity.setModelTitle(modelParent.getModelTitle());
				modelEntity.setModelId(modelParent.getModelId());
				if(ObjectUtil.isNotEmpty(getEntity(modelEntity))){
					throw new BusinessException("菜单标题重复");//抛异常事务回滚
				}
			}
			saveEntity(modelParent);
			RoleModelEntity roleModel = new RoleModelEntity();
			roleModel.setRoleId(mangerRoleId);
			roleModel.setModelId(Integer.parseInt(modelParent.getId()));
			roleModels.add(roleModel);
		} else {
			modelParent.setId(modelParentEntity.getId());
			updateEntity(modelParent);
		}
		if(ObjectUtil.isNotNull(modelParent.getModelChildList())&&modelParent.getModelChildList().size()>0){
			for (ModelEntity modelEntity : modelParent.getModelChildList()) {
				reModel(modelEntity, StringUtils.isBlank(parentIds)?modelParent.getId():parentIds+","+modelParent.getId(),mangerRoleId,roleModels,Integer.parseInt(modelParent.getId()));
			}
		}

	}

	@Deprecated
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void jsonToModel(String menuStr,int mangerRoleId,int modelId) {
		List<RoleModelEntity> roleModels = new ArrayList<>();
		if (StringUtils.isNotBlank(menuStr))
		{
			List<ModelEntity> list = JSONUtil.toList(menuStr, ModelEntity.class);
			for (ModelEntity modelParent : list) {
				ModelEntity entity = getEntity(modelParent);
				//如果存在此父级菜单则删除重新导入
				if(entity !=null&&(entity.getModelId()==null|| entity.getModelId() ==0)){
					deleteEntity(Integer.parseInt(entity.getId()));
				}
				reModel(modelParent,null,mangerRoleId,roleModels,modelId);
			}
			// 添加角色权限
			if (roleModels.size() > 0) {
				roleModelBiz.saveBatch(roleModels, roleModels.size());
			}
		}
	}

	@Transactional(rollbackFor = {Exception.class, Error.class})
	public void importModel(ModelEntity modelEntity, int mangerRoleId, String parentIds, Integer modelId){
		List<RoleModelEntity> roleModels = new ArrayList<>();
		//1.清空ID
		modelEntity.setId(null);
		modelEntity.setModelDatetime(new Timestamp(System.currentTimeMillis()));

		//2.设置modelId 与 parentIds
		if (modelId != null && modelId != 0) {
			modelEntity.setModelId(modelId);
			modelEntity.setModelParentIds(parentIds);
		} else {
			modelEntity.setModelId(null);
			modelEntity.setModelParentIds(null);
		}
		//3.插入SQL,这里不能使用雪花ID,只能使用自增长,因为使用雪花ID改动太大
		modelDao.insert(modelEntity);

		//4.创建新的角色实体
		RoleModelEntity roleModel = new RoleModelEntity();
		roleModel.setRoleId(mangerRoleId);
		roleModel.setModelId(Integer.parseInt(modelEntity.getId()));
		roleModels.add(roleModel);
		//5.添加角色权限
		if (roleModels.size() > 0) {
			roleModelBiz.saveBatch(roleModels, roleModels.size());
		}
		//5.如果有子级就递归
		if(ObjectUtil.isNotNull(modelEntity.getModelChildList()) && modelEntity.getModelChildList().size()>0){
			// curModelEntity当前遍历model   parentIds判断是否顶级，是存Id，否则保存modelId,父级ID,
			if (StringUtils.isBlank(parentIds) || modelId == null || modelId == 0) {
				parentIds = modelEntity.getId();
			}else {
				parentIds = parentIds+","+modelEntity.getId();
			}
			for (ModelEntity curModelEntity : modelEntity.getModelChildList()) {
				importModel(curModelEntity, mangerRoleId, parentIds,Integer.parseInt(modelEntity.getId()));
			}
		}
	}

	@Override
	public void updateEntity(ModelEntity model) {
		setParentId(model);
		modelDao.updateById(model);
		setChildParentId(model);
		//更新缓存
		modelDao.updateCache();
	}

	@Override
	public void saveEntity(ModelEntity model) {
		setParentId(model);
		modelDao.insert(model);
		//更新缓存
		modelDao.updateCache();
	}

	@Override
	public List<ModelEntity> queryChildList(ModelEntity modelEntity) {
		ModelEntity model = modelDao.selectOne(new QueryWrapper<>(modelEntity));
		if (model == null) {
			return null;
		}
		ModelEntity _model = new ModelEntity();
		_model.setModelId(model.getIntId());
		List<ModelEntity> list = modelDao.selectList(new QueryWrapper<>(_model));
		return list;
	}

	@Override
	public List<ModelEntity> queryAllChildList(ModelEntity modelEntity) {
		ModelEntity model = modelDao.selectOne(new QueryWrapper<>(modelEntity));
		if (model == null) {
			return null;
		}
		LambdaQueryWrapper<ModelEntity> wrapper = new LambdaQueryWrapper<>();
		wrapper.eq(ModelEntity::getDel, DeleteEnum.NOTDEL.toInt()).last(StrUtil.format("AND find_in_set({}, model_parent_ids) > 0", model.getId()));
		return modelDao.selectList(wrapper);
	}

	@Override
	@Transactional(rollbackFor = {Exception.class,Error.class})
	public void saveModel(ModelEntity model) {
		// 通过正常新增是不会有appId的，需要置空
		model.setAppId(null);
		save(model);
		ManagerEntity manager = BasicUtil.getManager();
		assert manager != null;
		// 保存菜单，并且为角色添加该新增菜单的权限（super不会给绑定的角色添加新增菜单的权限）
		// super可以直接获取全部权限
		if(!ManagerAdminEnum.SUPER.toString().equals(manager.getManagerAdmin())&& StringUtils.isNotEmpty(model.getId())){
			List<RoleModelEntity> roleModels = new ArrayList<>();
			for (String roleId : manager.getRoleIds().split(",")) {
				RoleModelEntity roleModel = new RoleModelEntity();
				roleModel.setModelId(Integer.parseInt(model.getId()));
				roleModel.setRoleId(Integer.parseInt(roleId));
				roleModels.add(roleModel);
			}
			roleModelBiz.saveBatch(roleModels, roleModels.size());
		}
		updateCache();
	}

	@Override
	public void delete(List<ModelEntity> modelEntities) {
		if (CollUtil.isEmpty(modelEntities)) {
			return;
		}
		// 获取当前菜单的子菜单
		ModelEntity modelEntity;
		List<ModelEntity> modelEntityList = new ArrayList<>();
		List<ModelEntity> tempList;
		// 循环取出模块集合
		for (ModelEntity model: modelEntities) {
			modelEntity = new ModelEntity();
			modelEntity.setId(model.getId());
			tempList = this.queryAllChildList(modelEntity);
			// 添加自身菜单
			modelEntityList.add(modelEntity);
			if (CollectionUtil.isNotEmpty(tempList)) {
				// 添加子集菜单
				modelEntityList.addAll(tempList);
			}
		}
		roleModelBiz.deleteByModelIds(modelEntityList);
		modelDao.deleteByIds(modelEntityList);
		// 刷新缓存
		modelDao.updateCache();
	}

	private void setParentId(ModelEntity model) {
		if(model.getModelId() != null && model.getModelId()>0) {
			ModelEntity _model = modelDao.selectById(model.getModelId());
			if(StringUtils.isEmpty(_model.getModelParentIds())) {
				model.setModelParentIds(_model.getId());
			} else {
				model.setModelParentIds(_model.getModelParentIds()+","+_model.getId());
			}
		}else {
			model.setModelParentIds(null);
			model.setModelId(null);
		}
	}
	private void setChildParentId(ModelEntity model) {
		ModelEntity _model=new ModelEntity();
		_model.setModelId(Integer.parseInt(model.getId()));
		List<ModelEntity> list = modelDao.query(_model);
		list.forEach(x->{
			if(StringUtils.isEmpty(model.getModelParentIds())) {
				x.setModelParentIds(model.getId());
			} else {
				x.setModelParentIds(model.getModelParentIds()+","+model.getId());
			}
			super.updateEntity(x);
			setChildParentId(x);
		});
	}

}
