package net.mingsoft.cms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import net.mingsoft.base.entity.BaseEntity;

/**
 * 说明书模板实体
 * 模板为 HTML 文件（占位符 {{字段名}}），下载时按产品规格数据实时渲染 PDF
 * @version
 * 版本号：1.0.0<br/>
 * 创建日期：2026-08-30<br/>
 */
@TableName("manual_template")
public class ManualTemplateEntity extends BaseEntity {

	private static final long serialVersionUID = 1788088000000L;

	/**
	 * 模板id（雪花）
	 */
	@TableId(type = IdType.ASSIGN_ID)
	private String id;

	/**
	 * 模板名称
	 */
	@TableField("template_name")
	private String templateName;

	/**
	 * 语言 cn/en（与中英文产品对应，绑定校验用）
	 */
	@TableField("template_lang")
	private String templateLang;

	/**
	 * 模板文件URL，如 /upload/1/manual/xxx.html
	 */
	@TableField("template_url")
	private String templateUrl;

	/**
	 * 模板文件字节数
	 */
	@TableField("template_size")
	private Integer templateSize;

	/**
	 * 模板中的占位符清单（逗号分隔，展示/校验用）
	 */
	@TableField("placeholders")
	private String placeholders;

	/**
	 * 状态 1启用 0停用
	 */
	@TableField("status")
	private String status;

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

	public String getTemplateName() {
		return templateName;
	}

	public void setTemplateName(String templateName) {
		this.templateName = templateName;
	}

	public String getTemplateLang() {
		return templateLang;
	}

	public void setTemplateLang(String templateLang) {
		this.templateLang = templateLang;
	}

	public String getTemplateUrl() {
		return templateUrl;
	}

	public void setTemplateUrl(String templateUrl) {
		this.templateUrl = templateUrl;
	}

	public Integer getTemplateSize() {
		return templateSize;
	}

	public void setTemplateSize(Integer templateSize) {
		this.templateSize = templateSize;
	}

	public String getPlaceholders() {
		return placeholders;
	}

	public void setPlaceholders(String placeholders) {
		this.placeholders = placeholders;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}
}
