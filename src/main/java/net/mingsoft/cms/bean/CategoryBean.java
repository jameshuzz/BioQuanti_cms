

package net.mingsoft.cms.bean;

import com.fasterxml.jackson.annotation.JsonFormat;
import net.mingsoft.cms.entity.CategoryEntity;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
* 文章实体
* 创建日期：2019-11-28 15:12:32<br/>
* 历史修订：<br/>
*/
public class CategoryBean extends CategoryEntity {

	/**
	 * 文章编号
	 */
	private String articleId;

	/**
	 * 文章更新时间
	 */
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
	private Date contentUpdateDate;

	/**
	 * 文章发布时间
	 */
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
	private Date contentDatetime;

	public Date getContentDatetime() {
		return contentDatetime;
	}

	public void setContentDatetime(Date contentDatetime) {
		this.contentDatetime = contentDatetime;
	}

	public Date getContentUpdateDate() {
		return contentUpdateDate;
	}

	public void setContentUpdateDate(Date contentUpdateDate) {
		this.contentUpdateDate = contentUpdateDate;
	}

	public String getArticleId() {
		return articleId;
	}

	public void setArticleId(String articleId) {
		this.articleId = articleId;
	}
}
