








package net.mingsoft.basic.entity;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import net.mingsoft.base.entity.BaseEntity;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * 网站基本信息实体类
 * @author 史爱华
 * @version
 * 版本号：100-000-000<br/>
 * 创建日期：2012-03-15<br/>
 * 历史修订：<br/>
 */
@TableName("app")
public class AppEntity extends BaseEntity {

	/**
	 * 网站运行状态
	 */
	private String appState;

	/**
	 * 应用名称
	 */
	private String appName;

	/**
	 * 应用描述
	 */
	private String appDescription;

	/**
	 * 应用logo
	 */
	private String appLogo;

	/**
	 * 应用图标
	 */
	private String appIco;

	/**
	 * 网站采用的模板风格
	 */

	private String appStyle;

	/**
	 * 网站关键字
	 */
	private String appKeyword;

	/**
	 * 网站版权信息
	 */
	private String appCopyright;

	/**
	 * 网站生成的文件夹
	 */
	private String appDir;

	/**
	 * 网站域名
	 */
	@TableField(updateStrategy = FieldStrategy.NOT_NULL)
	private String appUrl;

	/**
	 * 附加域名
	 */
	private String appUrls;

	/**
	 * 站点日期
	 */
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
	private Date appDatetime;

	/**
	 * 应用续费时间
	 */

	@DateTimeFormat(pattern = "yyyy-MM-dd")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
	private Date appPayDate;

	/**
	 * 应用费用清单
	 */
	private String appPay;


	public Date getAppPayDate() {
		return appPayDate;
	}

	public void setAppPayDate(Date appPayDate) {
		this.appPayDate = appPayDate;
	}

	public String getAppPay() {
		return appPay;
	}

	public void setAppPay(String appPay) {
		this.appPay = appPay;
	}

	public Date getAppDatetime() {
		return appDatetime;
	}

	public void setAppDatetime(Date appDatetime) {
		this.appDatetime = appDatetime;
	}

	/**
	 * 获取网站版权信息
	 *
	 * @return 返回网站版权信息
	 */
	public String getAppCopyright() {
		return appCopyright;
	}

	public String getAppDescription() {
		return appDescription;
	}

	/**
	 * 获取网站的关键字
	 *
	 * @return 返回网站关键字
	 */
	public String getAppKeyword() {
		return appKeyword;
	}


	public String getAppName() {
		return appName;
	}

	/**
	 * 获取网站使用的模板风格
	 *
	 * @return 返回网站使用的模板风格
	 */
	public String getAppStyle() {
		return appStyle;
	}

	/**
	 * 获取网站域名
	 */
	public String getAppUrl() {
		return appUrl;
	}

	/**
	 * 获取网站域名
	 * 目前appUrl只会有一个地址，但是删除方法怕影响其他地方
	 * 目前看这个方法也是返回第一个地址，所以方法暂时不动
	 */
	public String getAppHostUrl() {
		return appUrl;
	}

	public String getAppLogo() {
		return appLogo;
	}

	/**
	 * 设置网站版权信息
	 *
	 * @param appCopyright
	 */
	public void setAppCopyright(String appCopyright) {
		this.appCopyright = appCopyright;
	}

	public void setAppDescription(String appDescription) {
		this.appDescription = appDescription;
	}

    public String getAppId() {
        return id==null? "0":id;
    }

    public void setAppId(String id) {
        this.id = id;
    }

    /**
	 * 设置网站关键字
	 *
	 * @param appKeyword
	 */
	public void setAppKeyword(String appKeyword) {
		this.appKeyword = appKeyword;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	/**
	 * 设置网站使用的模板风格
	 *
	 * @param appStyle
	 */
	public void setAppStyle(String appStyle) {
		this.appStyle = appStyle;
	}

	/**
	 * 设置网站域名
	 *
	 * @param appUrl
	 */
	public void setAppUrl(String appUrl) {
		this.appUrl = appUrl;
	}


	public void setAppLogo(String appLogo) {
		this.appLogo = appLogo;
	}

	/**
	 * 获取网站图标
	 */
	public String getAppIco() {
		return appIco;
	}

	/**
	 * 设置网站图标
	 */
	public void setAppIco(String appIco) {
		this.appIco = appIco;
	}

	public String getAppDir() {
		return appDir;
	}

	public void setAppDir(String appDir) {
		this.appDir = appDir;
	}

	public String getAppState() {
		return appState;
	}

	public void setAppState(String appState) {
		this.appState = appState;
	}


	/**
	 * 获取附加域名
	 */
	public String getAppUrls() {
		return appUrls;
	}

	/**
	 * 设置附加域名
	 */
	public void setAppUrls(String appUrls) {
		this.appUrls = appUrls;
	}
}
