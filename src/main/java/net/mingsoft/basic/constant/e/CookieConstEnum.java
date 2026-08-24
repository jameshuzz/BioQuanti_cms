








package net.mingsoft.basic.constant.e;

import net.mingsoft.base.constant.e.BaseCookieEnum;

/**
 * cookie枚举类
 * @author ms dev group
 * @version 
 * 版本号：100-000-000<br/>
 * 创建日期：2012-03-15<br/>
 * 历史修订：<br/>
 */
public enum CookieConstEnum implements BaseCookieEnum{
	
	/**
	 * 分页cookie
	 */
	PAGENO_COOKIE("pageno_cookie"),
	
	/**
	 *上次访问地址
	 */
	BACK_COOKIE("back_cookie"),
	
	/**
	 * QQ登录保存当前用户点击地址的session
	 */
	BASIC_HIT("basic_hit");
	
	/**
	 * 设置CookieConst的常量
	 * @param attr 常量
	 */
	CookieConstEnum(String attr) {
		this.attr = attr;
	}
	
	private String attr;

	/**
	 * 返回该CookieConst常量的字符串表示
	 * @return 字符串
	 */
	@Override
	public String toString() {
		
		return attr;
	}
}
