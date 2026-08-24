



package net.mingsoft.mdiy.entity;


import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import net.mingsoft.base.entity.BaseEntity;

import java.util.Objects;

/**
 * 字典表实体
 * @version
 * 版本号：1<br/>
 * 创建日期：2017-8-12 14:22:36<br/>
 * 历史修订：2022-1-26 重写equals方法
 */
@TableName("mdiy_dict")
public class DictEntity extends BaseEntity {

	private static final long serialVersionUID = 1502518956351L;

	 /**
	  * 是否能够删除 0-能删除 1-不能删除
	  */
	 @TableField(whereStrategy = FieldStrategy.NEVER)
	 private int notDel;
	/**
	 * 数据值
	 */
	private String dictValue;
	/**
	 * 标签名
	 */
	private String dictLabel;
	/**
	 * 类型
	 */
	private String dictType;
	/**
	 * 描述
	 */
	private String dictDescription;
	/**
	 * 排序（升序）
	 */
	private Integer dictSort;
	/**
	 * 子业务关联
	 */
	private String isChild;
	 /**
	  * 启用状态
	  */
	 private Boolean dictEnable;
	/**
	 * 备注信息
	 */
	private String dictRemarks;


	/**
	 * 设置数据值
	 */
	public void setDictValue(String dictValue) {
		this.dictValue = dictValue;
	}

	/**
	 * 获取数据值
	 */
	public String getDictValue() {
		return this.dictValue;
	}

	/**
	 * 设置标签名
	 */
	public void setDictLabel(String dictLabel) {
		this.dictLabel = dictLabel;
	}

	/**
	 * 获取标签名
	 */
	public String getDictLabel() {
		return this.dictLabel;
	}

	/**
	 * 设置类型
	 */
	public void setDictType(String dictType) {
		this.dictType = dictType;
	}

	/**
	 * 获取类型
	 */
	public String getDictType() {
		return this.dictType;
	}

	/**
	 * 设置描述
	 */
	public void setDictDescription(String dictDescription) {
		this.dictDescription = dictDescription;
	}

	/**
	 * 获取描述
	 */
	public String getDictDescription() {
		return this.dictDescription;
	}

	/**
	 * 设置排序（升序）
	 */
	public void setDictSort(Integer dictSort) {
		this.dictSort = dictSort;
	}

	/**
	 * 获取排序（升序）
	 */
	public Integer getDictSort() {
		return this.dictSort;
	}


	/**
	 * 设置备注信息
	 */
	public void setDictRemarks(String dictRemarks) {
		this.dictRemarks = dictRemarks;
	}

	/**
	 * 获取备注信息
	 */
	public String getDictRemarks() {
		return this.dictRemarks;
	}
	/**
	 * 获取字典业务
	 */
	public String getIsChild() {
		return isChild;
	}
	/**
	 * 设置字典业务
	 */
	public void setIsChild(String isChild) {
		this.isChild = isChild;
	}
	 /**
	  * 设置启用状态
	  */
	 public void setDictEnable(Boolean dictEnable) {
		 this.dictEnable = dictEnable;
	 }
	 /**
	  * 获取启用状态
	  */
	 public Boolean getDictEnable() {
		 return this.dictEnable;
	 }
	 public int getNotDel() {
		 return notDel;
	 }

	 public void setNotDel(int notDel) {
		 this.notDel = notDel;
	 }

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		DictEntity that = (DictEntity) o;
		return  Objects.equals(dictType, that.dictType) && (Objects.equals(dictValue, that.dictValue) || Objects.equals(dictLabel, that.dictLabel));
	}

}
