








package net.mingsoft.base.entity;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import net.mingsoft.base.biz.SqlQueryWrapper;
import net.mingsoft.base.constant.e.DeleteEnum;
import net.mingsoft.base.util.SqlInjectionUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 *
 * @ClassName:  BaseEntity
 * @Description:TODO(基础实体类，其他所有实体都需要继承)
 * @date:   2018年3月19日 下午3:36:17
 *历史修订： 2022-02-17 setOrderBy() 修复orderBy字段可能存在的sql注入问题
 *
 */
public abstract class  BaseEntity implements Serializable{

	/**
	 * 创建用户编号
	 */
	@Schema(description = "创建用户")
	protected String createBy;
	/**
	 * 创建日期
	 */
	@Schema(description = "创建日期")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	protected Date createDate;

	/**
	 * 标记
	 */
	protected Integer del=0;

	/**
	 * 实体编号（唯一标识）
	 */
	@Schema(description = "实体编号（唯一标识）")
	protected String id;

	/**
	 * 备注
	 */
	@Schema(hidden = true)
	@TableField(exist = false)
	protected String remarks;

	/**
	 * 最后更新用户编号
	 */
	@Schema(description = "最后更新用户编号")
	protected String updateBy;

	/**
	 * 最后更新日期
	 */
	@Schema(description = "最后更新日期")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	protected Date updateDate;

	/**
	 * 自定义SQL where条件，需要配合对应dao.xml使用
	 */
	@JsonIgnore
	@TableField(exist = false)
	protected String sqlWhere;

	/**
	 * 自定义SQL where条件，需要配合对应dao.xml使用
	 */
	@JsonIgnore
	@TableField(exist = false)
	protected String sqlDataScope;

	/**
	 * 排序字段
	 */
	@Schema(hidden = true)
	@JsonIgnore
	@TableField(exist = false)
	protected String orderBy;

	/**
	 * 排序方式
	 */
	@Schema(hidden = true)
	@TableField(exist = false)
	protected String order;


	public String getCreateBy() {
		return createBy;
	}

	public void setCreateBy(String createBy) {
		this.createBy = createBy;
	}

	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public Integer getDel() {
		return del;
	}

	@JsonIgnore
	@JsonFormat(shape = JsonFormat.Shape.OBJECT)
	public void setDel(DeleteEnum del) {
		this.del = del.toInt();
	}

	public void setDel(Integer del) {
		this.del = del;
	}

	public String getId() {
		if(StringUtils.isEmpty(this.id) || this.id.equals("0")){
			return null;
		}
		return this.id;
	}

	/**
	 * 方便业务层获取id,为空返回null
	 * @return
	 */
	public Integer getIntegerId(){
		if(StringUtils.isEmpty(this.getId())){
			return null;
		}else {
			return Integer.parseInt(this.getId());
		}
	}

	/**
	 * 方便业务层获取id,为空返回0
	 * @return id
	 */
	public int getIntId(){
		if(StringUtils.isEmpty(this.getId())){
			return 0;
		}else {
			return Integer.parseInt(this.getId());
		}
	}


	/**
	 *
	 * @param id
	 */
	public void setId(String id) {
		if(StringUtils.isEmpty(id) || id.equals("0")){
			id = null;
		}
		this.id = id;
	}

	/**
	 * 方便业务层设置id
	 * @return
	 */
	@JsonIgnore
	public void setIntegerId(Integer id) {
		this.id = String.valueOf(id);
	}

	/**
	 * 方便业务层设置id
	 * @return
	 */
	@JsonIgnore
	public void setIntId(int id) {
		this.id = String.valueOf(id);
	}

	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}

	public String getUpdateBy() {
		return updateBy;
	}

	public void setUpdateBy(String updateBy) {
		this.updateBy = updateBy;
	}

	public Date getUpdateDate() {
		return updateDate;
	}

	public void setUpdateDate(Date updateDate) {
		this.updateDate = updateDate;
	}

	@JsonIgnore
	public String getSqlWhere() {
		return sqlWhere;
	}

	@JsonIgnore
	public List<SqlQueryWrapper.SqlWhere> getSqlWhereList() {
		List<SqlQueryWrapper.SqlWhere> sqlWhereList = new ArrayList<>();

		if(StringUtils.isNotBlank(sqlWhere)){
			sqlWhereList = JSONUtil.toList(sqlWhere,SqlQueryWrapper.SqlWhere.class);
			for (SqlQueryWrapper.SqlWhere where : sqlWhereList) {
				// 字段名规范检测
				SqlInjectionUtil.checkStandardTableColumnName(where.getField());
				String el = where.getEl();
				Object value = where.getValue();
				// like场景做兼容，直接对value值做处理，否则xml会产生差异写法(pg场景 ::text之类)
				if (SqlQueryWrapper.ElEnum.LIKE.getValue().equals(el)) {
					where.setValue("%" + value + "%");
				} else if (SqlQueryWrapper.ElEnum.LIKELEFT.getValue().equals(el)) {
					where.setValue("%" + value);
				} else if (SqlQueryWrapper.ElEnum.LIKERIGHT.getValue().equals(el)) {
					where.setValue(value + "%" );
				}
				if (!"time".equals(where.getType()) && !"date".equals(where.getType())) {
					continue;
				}
				String type = where.getType();

				// 处理值 避免时间、日期函数有差异 直接传入对应格式化的值 无需数据库层面处理
				boolean isRange = SqlQueryWrapper.ElEnum.RANGE.getValue().equals(where.getEl());
				if (("date".equals(type) || "time".equals(type)) && ObjectUtil.isNotEmpty(value)) {
					// 1. 统一将输入解析为 String 数组（单值也包装成单元素数组）
					List<String> rawValues = isRange ? JSONUtil.parseArray(value).toList(String.class) : List.of(value.toString());

					// 2. 转成Date(必须转Date String会依赖数据库是否宽容，ORACLE强类型限制)
					List<Date> dateList = rawValues.stream()
							.map(val -> (Date) DateUtil.parse(val))
							.collect(Collectors.toList());

					// 3. 日期、时间转成Date类型，回填对应的结构
					if (isRange) {
						where.setValue(dateList.toArray(new Date[0]));
					} else {
						where.setValue(dateList.get(0));
					}
				}
			}
		}
		return sqlWhereList;
	}

	public void setSqlWhere(String sqlWhere) {
		this.sqlWhere = sqlWhere;
	}

	public String getOrderBy() {
		return orderBy;
	}

	public void setOrderBy(String orderBy) {
		//若orderBy存在空格可能有sql注入问题
		if (orderBy != null){
			orderBy = orderBy.replaceAll(" ","");
		}
		this.orderBy = orderBy;
	}


	public String getSqlDataScope() {
		return sqlDataScope;
	}

	public void setSqlDataScope(String sqlDataScope) {
		this.sqlDataScope = sqlDataScope;
	}

	public String getOrder() {
		return order;
	}

	public void setOrder(String order) {
		this.order = order;
	}



}
