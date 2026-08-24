

package net.mingsoft.cms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import net.mingsoft.base.entity.BaseEntity;
/**
* 文章浏览记录实体
* 创建日期：2019-12-23 9:24:03<br/>
* 历史修订：<br/>
*/
@TableName("cms_history_log")
public class HistoryLogEntity extends BaseEntity {

private static final long serialVersionUID = 1577064243576L;

	@TableId(type = IdType.AUTO)
	private String id;

	@Override
	public String getId() {
		return id;
	}

	@Override
	public void setId(String id) {
		this.id = id;
	}
	/**
	* 文章编号
	*/
	private String contentId;
	/**
	* 浏览ip
	*/
	private String hlIp;
	/**
	* 用户idp
	*/
	private String peopleId;
	/**
	* 是否为移动端
	*/
	private Boolean hlIsMobile;


	/**
	* 设置文章编号
	*/
	public void setContentId(String contentId) {
	this.contentId = contentId;
	}

	/**
	* 获取文章编号
	*/
	public String getContentId() {
	return this.contentId;
	}
	/**
	* 设置浏览ip
	*/
	public void setHlIp(String hlIp) {
	this.hlIp = hlIp;
	}

	/**
	* 获取浏览ip
	*/
	public String getHlIp() {
	return this.hlIp;
	}

	public String getPeopleId() {
		return peopleId;
	}

	public void setPeopleId(String peopleId) {
		this.peopleId = peopleId;
	}

	/**
	* 设置是否为移动端
	*/
	public void setHlIsMobile(Boolean hlIsMobile) {
	this.hlIsMobile = hlIsMobile;
	}

	/**
	* 获取是否为移动端
	*/
	public Boolean getHlIsMobile() {
	return this.hlIsMobile;
	}
}
