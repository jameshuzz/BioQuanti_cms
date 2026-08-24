








package net.mingsoft.basic.action;


import cn.hutool.crypto.SecureUtil;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.mingsoft.base.entity.ResultData;
import net.mingsoft.basic.annotation.LogAnn;
import net.mingsoft.basic.bean.ManagerModifyPwdBean;
import net.mingsoft.basic.biz.IManagerBiz;
import net.mingsoft.basic.biz.IModelBiz;
import net.mingsoft.basic.constant.e.BusinessTypeEnum;
import net.mingsoft.basic.entity.ManagerEntity;
import net.mingsoft.basic.util.BasicUtil;
import net.mingsoft.basic.util.StringUtil;
import net.mingsoft.config.MSProperties;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

/**
 * 主界面控制层
 * @version
 * 版本号：100-000-000<br/>
 * 创建日期：2014-7-14<br/>
 * 历史修订：<br/>
 */
@Tag(name = "后端-基础接口")
@Controller
@RequestMapping("/${ms.manager.path}")
public class MainAction extends BaseAction {

	/**
	 * 模块业务层
	 */
	@Autowired
	private IModelBiz modelBiz;

	/**
	 * 管理员业务层
	 */
	@Autowired
	private IManagerBiz managerBiz;





	/**
	 * 加载后台主界面，并查询数据
	 * @param request 请求对象
	 * @return  主界面地址
	 */
	@Operation(summary =  "加载后台主界面，并查询数据")
	@GetMapping(value = {"/index","/"})
	public String index(HttpServletRequest request) {

		request.setAttribute("client", BasicUtil.getDomain()+"/"+ MSProperties.manager.path);
		return "/index";
	}

	@Hidden
	@GetMapping("/main")
	public String main(HttpServletRequest request) {
		return "/main";
	}




	/**
	 * 修改登录密码，若不填写密码则表示不修改
	 *
	 * @param request
	 *            请求
	 * @param response
	 *            响应
	 */
	@Operation(summary =  "修改登录密码，若不填写密码则表示不修改")
	@Parameters({
			@Parameter(name = "oldManagerPassword", description = "旧密码", required =  true, in = ParameterIn.QUERY),
			@Parameter(name = "newManagerPassword", description = "新密码", required =  true, in = ParameterIn.QUERY),
	})
	@LogAnn(title = "修改登录密码",businessType= BusinessTypeEnum.UPDATE)
	@PostMapping("/updatePassword")
	@ResponseBody
	public ResultData updatePassword(@ModelAttribute @Parameter(hidden = true) ManagerModifyPwdBean managerModifyPwdBean, HttpServletResponse response, HttpServletRequest request) {
		//获取新的密码
		String newManagerPassword = managerModifyPwdBean.getNewManagerPassword();
		//获取管理员信息
		ManagerEntity manager = BasicUtil.getManager();
		// 判断新密码和旧密码是否为空
		if (StringUtils.isBlank(newManagerPassword) || StringUtils.isBlank(managerModifyPwdBean.getOldManagerPassword())) {
			return ResultData.build().error(getResString("err.empty", this.getResString("managerPassword")));
		}

		//判断旧的密码是否正确
		if(!managerModifyPwdBean.getOldManagerPassword().equals(manager.getManagerPassword())){
			return ResultData.build().error(this.getResString("manager.password.old.err"));
		}
		// 判断新密码长度
		if (!StringUtil.checkLength(newManagerPassword, 6, 30)) {
			return ResultData.build().error(getResString("err.length", this.getResString("managerPassword"), "6", "30"));
		}
		//更改密码
		manager.setManagerPassword(SecureUtil.md5(newManagerPassword));
		//更新
		managerBiz.updateUserPasswordByUserName(manager);
		Subject subject = SecurityUtils.getSubject();
		subject.logout();
		return ResultData.build().success();
	}

	/**
	 * 退出系统
	 * @param request 请求对象
	 * @return true退出成功
	 */
	@Operation(summary =  "退出系统")
	@GetMapping("/loginOut")
	@ResponseBody
	public ResultData loginOut(HttpServletRequest request) {
		Subject subject = SecurityUtils.getSubject();
		subject.logout();
		return ResultData.build().success();
	}


}
