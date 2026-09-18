package net.mingsoft.cms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import net.mingsoft.base.entity.BaseEntity;

/**
 * 产品简介模板实体
 * 按栏目绑定的"产品简介"信息表骨架，文章编辑页自动注入富文本，编辑手工填右侧值
 * @version
 * 版本号：1.0.0<br/>
 * 创建日期：2026-09-18<br/>
 */
@TableName("bq_intro_tpl")
public class IntroTplEntity extends BaseEntity {

	private static final long serialVersionUID = 1789700000000L;

	/**
	 * 模板id（雪花）
	 */
	@TableId(type = IdType.ASSIGN_ID)
	private String id;

	/**
	 * 模板名称
	 */
	@TableField("tpl_name")
	private String tplName;

	/**
	 * 骨架HTML（<section class="bq-intro" data-tpl="id"><table class="bq-introtab">...）
	 */
	@TableField("content")
	private String content;

	/**
	 * 绑定栏目id（逗号分隔，绑定父栏目时其子栏目文章同样生效）
	 */
	@TableField("category_ids")
	private String categoryIds;

	/**
	 * 备注
	 */
	@TableField("remark")
	private String remark;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTplName() {
		return tplName;
	}

	public void setTplName(String tplName) {
		this.tplName = tplName;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getCategoryIds() {
		return categoryIds;
	}

	public void setCategoryIds(String categoryIds) {
		this.categoryIds = categoryIds;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}
}
