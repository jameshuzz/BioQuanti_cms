








package net.mingsoft.basic.biz.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.CaseInsensitiveMap;
import freemarker.template.Configuration;
import freemarker.template.TemplateException;
import net.mingsoft.base.biz.impl.BaseBizImpl;
import net.mingsoft.base.dao.IBaseDao;
import net.mingsoft.basic.biz.IAppBiz;
import net.mingsoft.basic.dao.IAppDao;
import net.mingsoft.basic.entity.AppEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 网站基本信息业务层实现类
 * @author 史爱华
 * @version
 * 版本号：100-000-000<br/>
 * 创建日期：2012-03-15<br/>
 * 历史修订：<br/>
 */
@Service("appBiz")
@Transactional
public class AppBizImpl extends BaseBizImpl<IAppDao,AppEntity> implements IAppBiz{

	/**
	 * 声明IAppDao持久化层
	 */
	@Autowired
	private IAppDao appDao;


	@Autowired
	private Configuration freeMarkerConfiguration;

	/**
	 * 获取应用持久化层
	 * @return appDao 返回应用持久化层
	 */
	@Override
	protected IBaseDao getDao() {
		
		return appDao;
	}

	@Override
	public int countByUrl(String websiteUrl) {
		
		return appDao.countByUrl(websiteUrl);
	}

	@Override
	public AppEntity getFirstApp() {
		List<AppEntity> appEntities = appDao.query(new AppEntity());
		for (AppEntity appEntity : appEntities) {
			// 获取第一个站点数据
			return appEntity;
		}

		// 返回null,说明一个站点都没有
		return null;
	}

	@Override
	public Map get(Map<String, Object> map) {
		List<Map<String, Object>> tagMaps = queryForList("SELECT tag_sql FROM mdiy_tag where tag_name = 'GLOBAL' and tag_type = 'single'");
		if (CollUtil.isEmpty(tagMaps)) {
			return null;
		}
		Map<String, Object> globalTag = new CaseInsensitiveMap<>();
		globalTag.putAll(tagMaps.get(0));
		String sqlTemplate = globalTag.get("tag_sql").toString();
		Map<String, Object> appMap = null;

		try {
			// 使用 StringTemplateLoader 处理模板字符串
			freemarker.template.Template template = new freemarker.template.Template(
					"globalSqlTemplate",
					new java.io.StringReader(sqlTemplate),
					freeMarkerConfiguration
			);
			String processedSql = FreeMarkerTemplateUtils.processTemplateIntoString(template, map);
			List<Map<String, Object>> appEntities = queryForListByNamedJdbc(processedSql, map);
			if (CollUtil.isEmpty(appEntities)) {
				return null;
			}
			// 默认取出第一个返回数据
			appMap = appEntities.get(0);
		} catch (IOException | TemplateException e) {
			e.printStackTrace();
		}
		return appMap;
	}

}
