



package net.mingsoft.mdiy.action;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import net.mingsoft.base.util.BundleUtil;
import net.mingsoft.basic.constant.e.SessionConstEnum;
import net.mingsoft.basic.util.BasicUtil;
import net.mingsoft.mdiy.constant.Const;
import net.mingsoft.mdiy.util.ConfigUtil;

import java.util.MissingResourceException;

/**
 * mdiy基础控制层
 * 创建日期：2018-10-24 8:44:33<br/>
 * 历史修订：<br/>
 */
public class BaseAction extends net.mingsoft.basic.action.BaseAction{


	@Override
	protected String getResString(String key) throws MissingResourceException{
		
		String str = "";
		try {
			str = super.getResString(key);
		} catch (MissingResourceException e) {
			//str = getLocaleString(key, Const.RESOURCES); 过期
			str = BundleUtil.getString(Const.RESOURCES,key);
		}

		return str;
	}


	/**
	 * 验证验证码
	 *
	 * @return 如果相同，返回true，否则返回false
	 */
	@Override
	protected boolean checkRandCode() {
		return checkRandCode( SessionConstEnum.CODE_SESSION.toString());
	}

	/**
	 * 验证验证码
	 *
	 * @param param   表单验证码参数名称
	 * @return 如果相同，返回true，否则返回false
	 */
	@Override
	protected boolean checkRandCode( String param) {
		// 检查是否存在配置，如果存在配置表示不是开源版本
		if(ObjectUtil.isEmpty(ConfigUtil.getEntity("后台开发配置"))) {
			return super.checkRandCode(param); //直接走开源的验证
		}
		boolean checkCode = ConfigUtil.getBoolean("后台开发配置", "webCheckCode", true);

		if (!checkCode) {
			return true;
		}
		String sessionCode = this.getRandCode();
		String requestCode = BasicUtil.getString(param);
		LOG.debug("session_code:" + sessionCode + " requestCode:" + requestCode);

		// 防止验证码为空，继续比对导致空指针报错
		if (StrUtil.isBlank(sessionCode)){
			return false;
		}
		if (sessionCode.equalsIgnoreCase(requestCode)) {
			// 验证码正确 删除session中的验证码
			BasicUtil.removeSession(SessionConstEnum.CODE_SESSION);
			return true;
		}
		return false;
	}
}
