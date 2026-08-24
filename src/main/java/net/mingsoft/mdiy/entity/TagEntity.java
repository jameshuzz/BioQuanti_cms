



package net.mingsoft.mdiy.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.SqlCondition;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import net.mingsoft.base.entity.BaseEntity;

 /**
 * 标签实体
 * 创建日期：2018-10-24 8:44:34<br/>
 * 历史修订：2021-05-02 合并tagsql<br/>
 */
 @TableName("mdiy_tag")
public class TagEntity extends BaseEntity {

	private static final long serialVersionUID = 1540341874663L;

	/**
	 * 标签名称
	 */
	@TableField(whereStrategy= FieldStrategy.NOT_EMPTY,condition = SqlCondition.LIKE)
	private String tagName;
	/**
	 * 标签类型
	 */
	private String tagType;

	 /**
	  * 是否能够删除 0-能删除 1-不能删除
	  */
	 @TableField(whereStrategy = FieldStrategy.NEVER)
	 private int notDel;

	 /**
	  * 标签sql
	  */
	 private String tagSql;

	 private String tagClass;
	 /**
	 * 描述
	 */
	private String tagDescription;

	 /**
	  * 模型Id
	  */
	private String modelId;

	 /**
	  * 站点Id
	  * 不归多租户管理，我们自己业务代码管理
	  */
	private String appId;



	/**
	 * 设置标签名称
	 */
	public void setTagName(String tagName) {
		this.tagName = tagName;
	}

	/**
	 * 获取标签名称
	 */
	public String getTagName() {
		return this.tagName;
	}
	/**
	 * 设置标签类型
	 */
	public void setTagType(String tagType) {
		this.tagType = tagType;
	}

	/**
	 * 获取标签类型
	 */
	public String getTagType() {
		return this.tagType;
	}
	/**
	 * 设置描述
	 */
	public void setTagDescription(String tagDescription) {
		this.tagDescription = tagDescription;
	}

	/**
	 * 获取描述
	 */
	public String getTagDescription() {
		return this.tagDescription;
	}

	 public String getTagSql() {
		 return tagSql;
	 }

	 public void setTagSql(String tagSql) {
		 this.tagSql = tagSql;
	 }

	 public String getTagClass() {
		 return tagClass;
	 }

	 public void setTagClass(String tagClass) {
		 this.tagClass = tagClass;
	 }

	 public String getModelId() {
		 return modelId;
	 }

	 public void setModelId(String modelId) {
		 this.modelId = modelId;
	 }

	 public int getNotDel() {
		 return notDel;
	 }

	 public void setNotDel(int notDel) {
		 this.notDel = notDel;
	 }

	 public String getAppId() {
		 return appId;
	 }

	 public void setAppId(String appId) {
		 this.appId = appId;
	 }
 }
