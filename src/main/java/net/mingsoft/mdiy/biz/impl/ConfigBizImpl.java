



package net.mingsoft.mdiy.biz.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import net.mingsoft.base.biz.impl.BaseBizImpl;
import net.mingsoft.base.dao.IBaseDao;
import net.mingsoft.basic.service.CacheConfigService;
import net.mingsoft.mdiy.bean.ModelJsonBean;
import net.mingsoft.mdiy.biz.IConfigBiz;
import net.mingsoft.mdiy.biz.IModelBiz;
import net.mingsoft.mdiy.dao.IConfigDao;
import net.mingsoft.mdiy.entity.ConfigEntity;
import net.mingsoft.mdiy.entity.ModelEntity;
import net.mingsoft.mdiy.util.ConfigUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 自定义配置管理持久化层
 *
 *
 * 创建日期：2021-3-25 11:42:09<br/>
 * 历史修订：<br/>
 */
@Service("mdiyConfigBizImpl")
@Transactional(rollbackFor = Exception.class)
public class ConfigBizImpl extends BaseBizImpl<IConfigDao, ConfigEntity> implements IConfigBiz {


	@Autowired
	private IConfigDao configDao;

	@Autowired
	@Lazy // 由于configBiz有在配置类注入，会较早被构造，而configBiz有依赖IModelBiz 导致IModelBiz过早构造没有被spring代理，增加lazy解决
	private IModelBiz modelBiz;

	@Autowired
	@Lazy
	private CacheConfigService cacheConfigService;

	@Override
	protected IBaseDao getDao() {
		
		return configDao;
	}

	@Override
	public boolean importConfig(String customType, ModelJsonBean modelJsonBean) {
		if (StringUtils.isBlank(modelJsonBean.getTitle())){
			return false;
		}
		// 判断导入的模型业务类型一致的情况下，判断模型名 或 表名是否存在
		ModelEntity modelEntity = new ModelEntity();
		modelEntity.setModelName(modelJsonBean.getTitle());
		modelEntity.setModelCustomType(customType);
		List<ModelEntity> modelEntities = modelBiz.query(modelEntity);
		//判断表名是否存在
		if (CollectionUtil.isNotEmpty(modelEntities)) {
			return false;
		}
		ModelEntity model = new ModelEntity();
		model.setModelName(modelJsonBean.getTitle());
		model.setModelCustomType(customType);
		model.setModelIdType(modelJsonBean.getId());

		Map<String, Object> json = new HashMap();
		json.put("html", modelJsonBean.getHtml());
		json.put("searchJson", modelJsonBean.getSearchJson());
		json.put("script", modelJsonBean.getScript());
		json.put("isWebSubmit", modelJsonBean.isWebSubmit());
		json.put("isWebCode", modelJsonBean.isWebCode());
		json.put("id", modelJsonBean.getId());
		// TODO: 2025/1/7 这里不会受ModelAop管理，不需要还原原始表名 
		json.put("tableName", modelJsonBean.getTableName());
		json.put("sql", modelJsonBean.getSql());

		json.put("form", modelJsonBean.getForm());
		model.setModelField(modelJsonBean.getField());
		model.setModelType("");
		model.setModelJson(JSONUtil.toJsonStr(json));
		model.setCreateDate(new Date());
		//保存自定义模型实体
		modelBiz.save(model);

		//保存自定义配置
		ConfigEntity configEntity = new ConfigEntity();
		configEntity.setConfigName(modelJsonBean.getTitle());
		// 设置模型类型，防止与全局自定义重复
		configEntity.setConfigType(customType);
		configEntity.setModelId(model.getId());
		this.save(configEntity);

		return true;
	}

	@Override
	public boolean updateConfig(String modelId, ModelJsonBean modelJsonBean) {
		if (StringUtils.isEmpty(modelId) || modelJsonBean == null) {
			return false;
		}
		ModelEntity modelEntity = modelBiz.getById(modelId);
		if (ObjectUtil.isNull(modelEntity)) {
			return false;
		}

		//模型名称必须唯一，需要进行查询判断
		ModelEntity model = new ModelEntity();
		model.setModelName(modelJsonBean.getTitle());
		model.setModelCustomType(modelEntity.getModelCustomType());
		ModelEntity oldModel = modelBiz.getByEntity(model);
		//判断表名是否存在
		if (ObjectUtil.isNotNull(oldModel) && !modelEntity.getId().equals(oldModel.getId())) {
			return false;
		}

		// 更新模型字段field
		modelBiz.updateModelField(modelJsonBean, modelEntity, "");

		// 更新自定义配置
		List<Map> mapList = JSONUtil.toList(JSONUtil.parseArray(modelJsonBean.getField()), Map.class);
		List<String> fieldList = mapList.stream().map(map -> StrUtil.toCamelCase(map.get("field").toString().toLowerCase())).collect(Collectors.toList());
		fieldList.add("linkId");
		fieldList.add("modelId");
		ConfigEntity configEntity = getOne(new QueryWrapper<ConfigEntity>().eq("model_id", modelId));

		Map<String, Object> map = JSONUtil.toBean(configEntity.getConfigData(), Map.class);

		if (CollUtil.isNotEmpty(map)) {
			Object[] keys = map.keySet().toArray();
			for (Object key : keys) {
				if (!fieldList.contains(key)) {
					map.remove(key);
				}
			}
			configEntity.setConfigName(modelJsonBean.getTitle());
			configEntity.setConfigData(JSONUtil.toJsonStr(map));
			updateById(configEntity);
			// 刷新缓存
			configDao.updateCache();
		}
		return true;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean delete(List<String> ids) {
		if (CollUtil.isEmpty(ids)) {
			return false;
		}
		for (String id : ids) {
			ModelEntity modelEntity = modelBiz.getById(id);
			if (modelEntity != null) {
				LambdaQueryWrapper<ConfigEntity> wrapper = new LambdaQueryWrapper<>();
				wrapper.eq(ConfigEntity::getModelId, id);
				ConfigEntity configEntity = this.getOne(wrapper);
				// 防止空数据导致删除异常
				if (ObjectUtil.isNull(configEntity)) {
					continue;
				}
				configDao.delete(new QueryWrapper<>(configEntity));
				//清除缓存
				ConfigUtil.removeEntity(modelEntity.getModelName(),configEntity.getAppId());
			}
		}
		modelBiz.removeByIds(ids);
		configDao.updateCache();
		return true;
	}

	@Override
	public ConfigEntity getByEntity(ConfigEntity configEntity) {
		return configDao.getByEntity(configEntity);
	}

	@Override
	public void reloadCache() {
		cacheConfigService.load();
	}
}
