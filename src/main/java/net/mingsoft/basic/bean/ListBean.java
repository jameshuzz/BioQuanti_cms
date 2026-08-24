








package net.mingsoft.basic.bean;

import com.github.pagehelper.PageInfo;

import java.util.List;

/**
 * 
 * 给返回json对象，支持分页
 * 
 * @version 版本号：<br/>
 *          创建日期：2016年6月2日<br/>
 *          历史修订：<br/>
 */
public class ListBean {

	private PageInfo page;

	private List list;

	public ListBean(List list) {
		this.list = list;
	}

	public ListBean(List list, PageInfo page) {
		this.page = page;
		this.list = list;
	}

	public PageInfo getPage() {
		return page;
	}

	public void setPage(PageInfo page) {
		this.page = page;
	}

	public List getList() {
		return list;
	}

	public void setList(List list) {
		this.list = list;
	}

}
