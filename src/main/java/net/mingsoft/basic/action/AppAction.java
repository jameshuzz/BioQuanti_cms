








package net.mingsoft.basic.action;

import cn.hutool.core.util.ObjectUtil;
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
import net.mingsoft.basic.biz.IAppBiz;
import net.mingsoft.basic.constant.e.BusinessTypeEnum;
import net.mingsoft.basic.constant.e.CookieConstEnum;
import net.mingsoft.basic.entity.AppEntity;
import net.mingsoft.basic.entity.ManagerEntity;
import net.mingsoft.basic.util.BasicUtil;
import net.mingsoft.basic.util.StringUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

/**
 * 网站基本信息控制层
 *
 * @version 版本号：100-000-000<br/>
 *          创建日期：2014-07-14<br/>
 *          历史修订：<br/>
 */
@Tag(name = "后端-基础接口")
@Controller
@RequestMapping("/${ms.manager.path}/basic/app")
public class AppAction extends BaseAction {

	/**
	 * appBiz业务层的注入
	 */
	@Autowired
	private IAppBiz appBiz;

	/**
	 * 跳转到修改页面
	 *
	 * @param mode
	 *            ModelMap实体对象
	 * @param appId
	 *            站点id
	 * @param request
	 *            请求对象
	 * @return 站点修改页面
	 */
	@Hidden
	@GetMapping(value = "/app")
	@RequiresPermissions("basic:app:view")
	public String app(HttpServletRequest request) {
		return "/basic/app/app";

	}

    /**
     * 获取站点信息
     * @param appId
     * @return
     */
    @Operation(summary =  "获取站点信息")
    @Parameter(name = "appId", description = "站点ID", required = false, in = ParameterIn.QUERY)
    @GetMapping(value = "/get")
    @ResponseBody
    public ResultData get() {
			// appId为空，表示查询当前站
		String appId = BasicUtil.getString("appId", "");
		AppEntity app = null;
        //若有appid直接根据appId查询
        if (StringUtils.isBlank(appId)) {
            app = BasicUtil.getApp();
            if(app!=null) {
                //防止session再次压入appId
                if(BasicUtil.getSession("appId")==null){
                    BasicUtil.setSession("appId",app.getAppId());
                }
            } else {
                appId = (String) BasicUtil.getSession("appId");
                app =  appBiz.getById(appId);
            }
        } else {
            app =  appBiz.getById(appId);
        }
		return ResultData.build().success(app);

    }

	/**
	 * 更新站点信息
	 *
	 * @param mode
	 *            ModelMap实体对象
	 * @param app
	 *            站点对象
	 * @param request
	 *            请求对象
	 * @param response
	 *            相应对象
	 */
	@Operation(summary = "更新站点信息")
	@Parameters({
		@Parameter(name = "id", description = "站点id", required =  true, in = ParameterIn.QUERY),
		@Parameter(name = "appName", description = "应用名称", required =  true, in = ParameterIn.QUERY),
		@Parameter(name = "appDir", description = "网站生成目录", required =  true, in = ParameterIn.QUERY),
		@Parameter(name = "appStyle", description = "网站采用的模板风格", required =  false, in = ParameterIn.QUERY),
		@Parameter(name = "appDescription", description = "应用描述", required =  false, in = ParameterIn.QUERY),
		@Parameter(name = "appLogo", description = "应用logo", required =  false, in = ParameterIn.QUERY),
		@Parameter(name = "appKeyword", description = "网站关键字", required =  false, in = ParameterIn.QUERY),
		@Parameter(name = "appCopyright", description = "网站版权信息", required =  false, in = ParameterIn.QUERY)
	})
	@PostMapping("/update")
	@LogAnn(title = "更新站点信息",businessType = BusinessTypeEnum.UPDATE)
	@RequiresPermissions("basic:app:update")
	@ResponseBody
	public ResultData update(@ModelAttribute @Parameter(hidden = true) AppEntity app,ModelMap mode, HttpServletRequest request,
							 HttpServletResponse response) {
		mode.clear();
		// 获取Session值
		ManagerEntity managerSession = BasicUtil.getManager();
		if (managerSession == null) {
			return ResultData.build().error();
		}
		mode.addAttribute("managerSession", managerSession);

		//验证appLogo是否合法
		if (app.getAppLogo() == null || !app.getAppLogo().matches("^\\[.{1,}]$")) {
			app.setAppLogo("");
		}
		//验证appIco是否合法
		if (app.getAppIco() == null || !app.getAppIco().matches("^\\[.{1,}]$")) {
			app.setAppIco("");
		}
		//验证重复
		if(super.validated("app", "app_dir", app.getAppDir(), app.getId(), "id")){
			return ResultData.build().error(getResString("err.exist", this.getResString("app.dir")));
		}
		if(StringUtils.isBlank(app.getAppDir())){
			return ResultData.build().error(getResString("err.empty", this.getResString("app.dir")));
		}
		if(!StringUtil.checkLength(app.getAppDir()+"", 0, 50)){
			return ResultData.build().error(getResString("err.length", this.getResString("app.dir"), "0", "10"));
		}
		// 判断站点数据的合法性
		// 获取cookie
		String cookie = BasicUtil.getCookie(CookieConstEnum.PAGENO_COOKIE);
		int pageNo = 1;
		// 判断cookies是否为空
		if (StringUtils.isNotBlank(cookie) && Integer.valueOf(cookie) > 0) {
			pageNo = Integer.valueOf(cookie);
		}
		mode.addAttribute("pageNo", pageNo);
		ResultData resultData = ResultData.build();
		if (!checkForm(app, resultData)) {
			return resultData;
		}
		if (StringUtils.isNotBlank(app.getAppLogo())) {
			app.setAppLogo(app.getAppLogo().replace("|", ""));
		}
		app.setAppUrl(BasicUtil.getUrl());
		BasicUtil.cleanApp();
		appBiz.updateById(app);
		appBiz.updateCache();
		// 站群情况 更新MapCache中的app信息；不更新站群情况获取的还是旧的app信息
		if(BasicUtil.getWebsiteApp()!=null){
			BasicUtil.setWebsiteApp(app);
		}
		return ResultData.build().success();
	}

	@Operation(summary =  "刷新站点缓存")
	@PostMapping("/refreshCache")
	@ResponseBody
	public ResultData refreshCache(HttpServletRequest request) {
		BasicUtil.cleanApp();
		appBiz.updateCache();
		// 站群情况 更新MapCache中的app信息；
		if (BasicUtil.getWebsiteApp()!=null) {
			BasicUtil.setWebsiteApp(appBiz.getById(BasicUtil.getWebsiteAppId()));
		}
		return ResultData.build().success();
	}

	/**
	 * 判断站点域名的合法性
	 *
	 * @param app
	 *            要验证的站点信息
	 * @param resultData
	 *            resultData对象
	 */
	private boolean checkForm(AppEntity app, ResultData resultData) {

		/*
		 * 判断数据的合法性
		 */
		if (StringUtils.isNotBlank(app.getAppKeyword()) && !StringUtil.checkLength(app.getAppKeyword(), 0, 1000)) {
			resultData.error(getResString("err.length", this.getResString("appKeyword"), "0", "1000"));
			return false;
		}
		if (StringUtils.isNotBlank(app.getAppCopyright()) && !StringUtil.checkLength(app.getAppCopyright(), 0, 1000)) {
			resultData.error(getResString("err.length", this.getResString("appCopyright"), "0", "1000"));
			return false;
		}
		if (StringUtils.isNotBlank(app.getAppDescription()) && !StringUtil.checkLength(app.getAppDescription(), 0, 1000)) {
			resultData.error(getResString("err.length", this.getResString("appDescrip"), "0", "1000"));
			return false;
		}
		if (!StringUtil.checkLength(app.getAppName(), 1, 50)) {
			resultData.error(getResString("err.length", this.getResString("appTitle"), "1", "50"));
			return false;
		}
		if (StringUtils.isNotBlank(app.getAppStyle()) && !StringUtil.checkLength(app.getAppStyle(), 1, 30)) {
			resultData.error(getResString("err.length", this.getResString("appStyle"), "1", "30"));
			return false;
		}
		if(ObjectUtil.isNotNull(app.getAppHostUrl())){
			if (!StringUtil.checkLength(app.getAppHostUrl(), 10, 150)) {
				resultData.error(getResString("err.length", this.getResString("appUrl"), "10", "150"));
				return false;
			}
		}
		return true;
	}

	/**
	 * 判断是否有重复的域名
	 *
	 * @param request
	 *            请求对象
	 * @return true:重复,false:不重复
	 */
	@Operation(summary =  "判断是否有重复的域名")
	@Parameter(name = "appUrl", description = "网站域名", required =  true, in = ParameterIn.QUERY)
	@GetMapping("/checkUrl")
	@ResponseBody
	public ResultData checkUrl(HttpServletRequest request) {
		if (request.getParameter("appUrl") != null) {
			if (appBiz.countByUrl(request.getParameter("appUrl")) > 0) {
				return ResultData.build().success();
			} else {
				return ResultData.build().error();
			}
		} else {
			return ResultData.build().error();
		}

	}
}
