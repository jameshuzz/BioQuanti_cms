








package net.mingsoft.basic.biz.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import net.mingsoft.base.biz.impl.BaseBizImpl;
import net.mingsoft.base.dao.IBaseDao;
import net.mingsoft.basic.biz.IRoleModelBiz;
import net.mingsoft.basic.dao.IRoleModelDao;
import net.mingsoft.basic.entity.ModelEntity;
import net.mingsoft.basic.entity.RoleModelEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;


/**
 * 角色模块关联业务层接口实现类
 * @author 张敏
 * @version
 * 版本号：100-000-000<br/>
 * 创建日期：2012-03-15<br/>
 * 历史修订：<br/>
 */
@Service("roleModelBiz")
@Transactional
public class RoleModelBizImpl extends BaseBizImpl<IRoleModelDao, RoleModelEntity> implements IRoleModelBiz {

	/**
	 * 角色模块关联持久化层
	 */
	@Autowired
	private IRoleModelDao roleModelDao;

	/**
	 * 获取角色模块持久化层
	 * @return roleModelDao 返回角色模块持久化层
	 */
	@Override
	public IBaseDao getDao() {
		
		return roleModelDao;
	}

	@Override
	public void saveEntity(List<RoleModelEntity> roleModelList){
		
		roleModelDao.saveEntity(roleModelList);
	}

	@Override
	public void updateEntity(List<RoleModelEntity> roleModelList){
		
		roleModelDao.updateEntity(roleModelList);
	}

	@Override
	public List<RoleModelEntity> queryByRoleId(int roleId) {
		
		return roleModelDao.queryByRoleId(roleId);
	}

	@Override
	public void deleteByRoleId(int roleId) {
		roleModelDao.deleteByRoleId(roleId);
	}

	@Override
	public void deleteByRoleIds(int[] ids) {
		roleModelDao.deleteByRoleIds(ids);
	}


	@Override
	public void deleteByModelIds(List<ModelEntity> models) {
		// 做空判断，防止删除全部数据或者报错
		if (CollUtil.isEmpty(models)) {
			return;
		}
		// 取出所有的modelId
		List<String> modelIds = models.stream().map(ModelEntity::getId).collect(Collectors.toList());
		// 删除关联表
		roleModelDao.delete(new LambdaQueryWrapper<RoleModelEntity>().in(RoleModelEntity::getModelId, modelIds));
	}
}
