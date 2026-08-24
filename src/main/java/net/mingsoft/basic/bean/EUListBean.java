








package net.mingsoft.basic.bean;

import java.util.List;

/**
 *
 * 给返回json对象，支持分页
 *
 * @version 版本号：<br/>
 *          创建日期：2016年6月2日<br/>
 *          历史修订：<br/>
 */
public class EUListBean {

	private int total;

	private List rows;

	// 需要一个空的构造器防止fastJson初始化报错
	public EUListBean() {

	}

	public EUListBean(List rows, int total) {
		this.total = total;
		this.rows = rows;
	}


	public int getTotal() {
		return total;
	}


	public void setTotal(int total) {
		this.total = total;
	}


	public List getRows() {
		return rows;
	}


	public void setRows(List rows) {
		this.rows = rows;
	}

}
