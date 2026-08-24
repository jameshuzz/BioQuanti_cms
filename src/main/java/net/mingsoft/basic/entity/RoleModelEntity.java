








package net.mingsoft.basic.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import net.mingsoft.base.entity.BaseEntity;

import java.util.Date;

/**
 * 角色与模块关联表
 * @author 铭软团队
 * @version 
 * 版本号：100-000-000<br/>
 * 创建日期：2012-03-15<br/>
 * 历史修订：<br/>
 */
@TableName("role_model")
public class RoleModelEntity extends BaseEntity {

	//关联表忽略6个常用字段
	@TableField(exist = false)
	protected String id;
	@TableField(exist = false)
	protected int del;
	@TableField(exist = false)
	protected int createBy;
	@TableField(exist = false)
	protected Date createDate;
	@TableField(exist = false)
	protected int updateBy;
	@TableField(exist = false)
	protected Date updateDate;


	/**
	 * 模块编号
	 */
	private int modelId;
	
	/**
	 * 角色编号
	 */
	private int roleId;

	/**
	 *获取 modelId
	 * @return modelId
	 */
	public int getModelId() {
		return modelId;
	}

	/**
	 *设置modelId
	 * @param modelId
	 */
	public void setModelId(int modelId) {
		this.modelId = modelId;
	}

	/**
	 *获取 roleId
	 * @return roleId
	 */
	public int getRoleId() {
		return roleId;
	}

	/**
	 *设置roleId
	 * @param roleId
	 */
	public void setRoleId(int roleId) {
		this.roleId = roleId;
	}
}
