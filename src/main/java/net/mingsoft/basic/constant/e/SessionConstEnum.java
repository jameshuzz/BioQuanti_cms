








package net.mingsoft.basic.constant.e;

import net.mingsoft.base.constant.e.BaseSessionEnum;

/**
 * session枚举
 *
 * @author ms dev group
 * @version 版本号：100-000-000<br/>
 *          创建日期：2012-03-15<br/>
 *          历史修订：<br/>
 */
public enum SessionConstEnum implements BaseSessionEnum {

	/**
	 * 验证码session
	 */
	CODE_SESSION("rand_code");

	/**
	 * 设置session常量
	 *
	 * @param attr
	 *            常量
	 */
	SessionConstEnum(String attr) {
		this.attr = attr;
	}

	private String attr;

	/**
	 * 返回SessionConst常量的字符串表示
	 *
	 * @return 字符串
	 */
	@Override
	public String toString() {
		
		return attr;
	}

}
