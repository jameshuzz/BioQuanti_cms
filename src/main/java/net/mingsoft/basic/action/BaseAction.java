








package net.mingsoft.basic.action;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.CaseInsensitiveMap;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import jakarta.servlet.http.HttpServletRequest;
import net.mingsoft.base.util.SqlInjectionUtil;
import net.mingsoft.basic.biz.IAppBiz;
import net.mingsoft.basic.constant.Const;
import net.mingsoft.basic.constant.e.CookieConstEnum;
import net.mingsoft.basic.constant.e.SessionConstEnum;
import net.mingsoft.basic.entity.AppEntity;
import net.mingsoft.basic.strategy.IModelStrategy;
import net.mingsoft.basic.util.BasicUtil;
import net.mingsoft.basic.util.ConfigUtil;
import net.mingsoft.config.MSProperties;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;

/**
 * 基础应用层的父类base
 *
 * @version 版本号：100-000-000<br/>
 *          创建日期：2015-7-19<br/>
 *          历史修订：<br/>
 */
//@Tag(name = "基础应用层的父类base")
public abstract class BaseAction extends net.mingsoft.base.action.BaseAction {
	/**
	 * appBiz业务层的注入
	 */
	@Autowired
	private IAppBiz appBiz;

	@Autowired
	private IModelStrategy modelStrategy;

	@Value("${ms.mwebsite:table:cms_category,cms_content,mdiy_dict,mdiy_page,role}")
	private String tables;


//	@Value("${ms.manager.check-code:true}")
//	private Boolean checkCode;


	@Override
	protected String getResString(String key) {
		
		String str = "";
		try {
			str = super.getResString(key);
		} catch (MissingResourceException e) {
			str = getLocaleString(key, Const.RESOURCES);
		}
		return str;
	}


	/**
	 * 验证验证码
	 *
	 * @return 如果相同，返回true，否则返回false
	 */
	protected boolean checkRandCode() {
		return checkRandCode( SessionConstEnum.CODE_SESSION.toString());
	}

	/**
	 * AES解密字符串,key值为当前应用编号
	 *
	 * @param request HttpServletRequest对象
	 * @param str     需要解密的字符串
	 * @return 返回解密后的字符串
	 */
	protected String decryptByAES(HttpServletRequest request, String str) {
		// 这里存在一个糊涂工具的bug必须先用变量保存变量再返回
		String _str = SecureUtil.aes(SecureUtil.md5(BasicUtil.getApp().getAppId() + "").substring(16).getBytes())
				.decryptStr(str);
		return _str;
	}

	/**
	 * AES加密字符串,key值为当前应用编号
	 *
	 * @param request HttpServletRequest对象
	 * @param str     需要加密的字符串
	 * @return 返回加密后的字符串
	 */
	protected String encryptByAES(HttpServletRequest request, String str) {
		// 这里存在一个糊涂工具的bug必须先用变量保存变量再返回
		String _str = SecureUtil.aes(SecureUtil.md5(BasicUtil.getApp().getAppId() + "").substring(16).getBytes())
				.encryptHex(str);
		return _str;
	}

	/**
	 * 获取验证码
	 *
	 * @return 返回验证码，获取不到返回null
	 */
	protected String getRandCode() {
		return (String) BasicUtil.getSession(SessionConstEnum.CODE_SESSION);
	}

	/**
	 * 返回重定向
	 *
	 * @param flag    true:提供给springMVC返回，false:只是获取地址
	 * @return 返回重定向后的地址
	 */
	protected String redirectBack( boolean flag) {
		if (flag) {
			return "redirect:" + BasicUtil.getCookie(CookieConstEnum.BACK_COOKIE);
		} else {
			return BasicUtil.getCookie(CookieConstEnum.BACK_COOKIE);
		}

	}

	/**
	 * 验证验证码
	 *
	 * @param param   表单验证码参数名称
	 * @return 如果相同，返回true，否则返回false
	 */
	protected boolean checkRandCode( String param) {
		boolean checkCode = ConfigUtil.getBoolean(Const.MANAGER_CONFIG, "managerCheckCode", MSProperties.manager.checkCode);
		if(!checkCode){
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


	/**
	 * 移除url参数
	 *
	 * @param request
	 * @param fitlers 需要移除的字段名称
	 */
	@Deprecated
	protected void removeUrlParams(HttpServletRequest request, String[] fitlers) {
		request.setAttribute(Const.PARAMS, BasicUtil.assemblyRequestUrlParams(fitlers));
	}

	/**
	 * 适用于insert save数据时进行唯一性判断
	 * 判断指定字段在数据库是否已经存在
	 * @param tableName 表名
	 * @param fieldName 字段名
	 * @param fieldValue 字段值
	 * @return
	 */
	protected boolean validated(String tableName,String fieldName, String fieldValue) {
		SqlInjectionUtil.filterContent(tableName,fieldName,fieldValue);
		// 构建预处理SQL
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT * FROM ").append(tableName).append(" WHERE ").append(fieldName).append(" = ?");
		AppEntity websiteApp = BasicUtil.getWebsiteApp();
		List<String> tableNames = StrUtil.split(ConfigUtil.getString("站群配置", "siteTables", tables), ",");
		if (ObjectUtil.isNotNull(websiteApp) && tableNames.contains(tableName)) {
			sql.append(" AND ").append("APP_ID = ").append(websiteApp.getAppId());
		}
		List<Map<String, Object>> list = appBiz.queryForList(sql.toString(), fieldValue);
		return CollUtil.isNotEmpty(list);
	}

	/**
	 * 适用于update 更新 数据时进行唯一性判断
	 * 判断指定字段在数据库是否已经存在
	 * 主键id用来防止跟自身字段验证重复
	 * @param tableName 表名
	 * @param fieldName 字段名
	 * @param fieldValue 字段值
	 * @param id 要更新的主键id
	 * @param idName 要更新的主键名称
	 * @return
	 */
	protected boolean validated(String tableName, String fieldName, String fieldValue, String id,String idName) {
		SqlInjectionUtil.filterContent(tableName,fieldName,fieldValue);
		// 构建预处理SQL
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT * FROM ").append(tableName).append(" WHERE ").append(fieldName).append(" = ?");
		AppEntity websiteApp = BasicUtil.getWebsiteApp();
		List<String> tableNames = StrUtil.split(ConfigUtil.getString("站群配置", "siteTables", tables), ",");
		if (ObjectUtil.isNotNull(websiteApp) && tableNames.contains(tableName)) {
			sql.append(" AND ").append("APP_ID = ").append(websiteApp.getAppId());
		}
		List<Map<String, Object>> list = appBiz.queryForList(sql.toString(), fieldValue);
		if (CollUtil.isNotEmpty(list)) {
			//更新时判断是否是本身
			if(list.size() == 1){
				CaseInsensitiveMap<String, Object> _map = new CaseInsensitiveMap<>(list.get(0));
				return !id.equals(_map.get(idName).toString());
			}
			return true;
		}
		return false;
	}

	/**
	 * 权限判断,若没有主权限,就以业务权限标识为准
	 * 场景如：自定义业务表单的功能，与通过自定义业务菜单复制出去的菜单功能，如果有总的业务表单管理功能(业务数据权限)，就可以管理所有菜单链接的自定义业务表单管理功能
	 * @param masterPermission 主权限
	 * @param permission 业务权限标识
	 * @return
	 */
	protected boolean hasPermissions(String masterPermission,String permission) {
		return SecurityUtils.getSubject().isPermitted(masterPermission) || SecurityUtils.getSubject().isPermitted(permission);
	}


}
