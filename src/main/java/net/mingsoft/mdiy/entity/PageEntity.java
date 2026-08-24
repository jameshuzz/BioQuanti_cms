



package net.mingsoft.mdiy.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.SqlCondition;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import net.mingsoft.base.entity.BaseEntity;

 /**
 * 自定义页面表实体
 * @author 蓝精灵
 * @version
 * 版本号：1<br/>
 * 创建日期：2017-8-11 14:01:54<br/>
 * 历史修订：<br/>
 */
@TableName("mdiy_page")
public class PageEntity extends BaseEntity {

	private static final long serialVersionUID = 1502431314331L;

	/**
	 * 自定义页面绑定模板的路径
	 */
	private String pagePath;
	/**
	 * 自定义页面标题
	 */
	@TableField(whereStrategy= FieldStrategy.NOT_EMPTY,condition = SqlCondition.LIKE)
	private String pageTitle;

	 /**
	  * 是否能够删除 0-能删除 1-不能删除
	  */
	 @TableField(whereStrategy = FieldStrategy.NEVER)
	 private int notDel;

	/**
	 * 自定义页面访问路径
	 */
	private String pageKey;
	 /**
	  * 页面类型
	  */
	 private String pageType;
	 /**
	  * 启用状态
	  */
	 private Boolean pageEnable;
	/**
	 * 设置自定义页面绑定模板的路径
	 */
	public void setPagePath(String pagePath) {
		this.pagePath = pagePath;
	}

	/**
	 * 获取自定义页面绑定模板的路径
	 */
	public String getPagePath() {
		return this.pagePath;
	}

	/**
	 * 设置自定义页面标题
	 */
	public void setPageTitle(String pageTitle) {
		this.pageTitle = pageTitle;
	}

	/**
	 * 获取自定义页面标题
	 */
	public String getPageTitle() {
		return this.pageTitle;
	}

	/**
	 * 设置自定义页面访问路径
	 */
	public void setPageKey(String pageKey) {
		this.pageKey = pageKey;
	}

	/**
	 * 获取自定义页面访问路径
	 */
	public String getPageKey() {
		return this.pageKey;
	}
	 /**
	  * 设置页面类型
	  */
	 public void setPageType(String pageType) {
		 this.pageType = pageType;
	 }

	 /**
	  * 获取页面类型
	  */
	 public String getPageType() {
		 return this.pageType;
	 }

	 public int getNotDel() {
		 return notDel;
	 }

	 public void setNotDel(int notDel) {
		 this.notDel = notDel;
	 }
	 /**
	  * 获取自定义页面状态
	  */
	 public Boolean getPageEnable() {
		 return this.pageEnable;
	 }
	 /**
	  * 设置自定义页面状态
	  */
	 public void setPageEnable(Boolean pageEnable) {
		 this.pageEnable = pageEnable;
	 }
}
