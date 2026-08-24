








package net.mingsoft.basic.constant.e;

import net.mingsoft.base.constant.e.BaseEnum;

/**
 * 管理员类型枚举
 * @author ms dev group
 * @version
 * 版本号：100-000-000<br/>
 * 创建日期：2012-03-15<br/>
 * 历史修订：<br/>
 * 2022-1-4 增加SUPERADMIN枚举,用于区分站群管理员权限
 */
public enum ManagerAdminEnum implements BaseEnum {

	/**
	 * 超级管理员
	 */
	SUPER("super"),
	/**
	 * 站群超级管理员,只有在启用了站群才有用
	 */
	SUPERADMIN("superadmin");


	/**
	 * 枚举类型
	 */
	private String code;

	/**
	 * 构造方法
	 * @param code 传入的枚举类型
	 */
	ManagerAdminEnum(String code) {
		this.code = code;
	}

	/**
	 * 实现父类方法转换为整形
	 */
	@Override
	public int toInt() {
		return Integer.valueOf(code.toString());
	}

	@Override
	public String toString() {
		return code;
	}
}
