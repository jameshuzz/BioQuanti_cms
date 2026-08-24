



package net.mingsoft.mdiy.biz.impl;

import net.mingsoft.base.biz.impl.BaseBizImpl;
import net.mingsoft.base.dao.IBaseDao;
import net.mingsoft.mdiy.biz.IPageBiz;
import net.mingsoft.mdiy.dao.IPageDao;
import net.mingsoft.mdiy.entity.PageEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 自定义页面表管理持久化层
 * @author 蓝精灵
 * @version
 * 版本号：1<br/>
 * 创建日期：2017-8-11 14:01:54<br/>
 * 历史修订：<br/>
 */
 @Service("pageBizImpl")
public class PageBizImpl extends BaseBizImpl<IPageDao, PageEntity> implements IPageBiz {


	@Autowired
	private IPageDao pageDao;


		@Override
	protected IBaseDao getDao() {
		
		return pageDao;
	}
}
