






package net.mingsoft.basic.action;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import net.mingsoft.base.entity.ResultData;
import net.mingsoft.basic.bean.EUListBean;
import net.mingsoft.basic.bean.LogBean;
import net.mingsoft.basic.biz.ILogBiz;
import net.mingsoft.basic.constant.e.BusinessTypeEnum;
import net.mingsoft.basic.entity.LogEntity;
import net.mingsoft.basic.util.BasicUtil;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * 系统日志管理控制层
 * 创建日期：2020-11-21 9:41:34<br/>
 * 历史修订：<br/>
 */
@Tag(name = "后端-基础接口")
@Controller("basicLogAction")
@RequestMapping("/${ms.manager.path}/basic/log")
public class LogAction extends BaseAction{


	/**
	 * 注入系统日志业务层
	 */
	@Autowired
	private ILogBiz logBiz;

	/**
	 * 返回主界面index
	 */
	@Hidden
	@GetMapping("/index")
	@RequiresPermissions("basic:log:view")
	public String index(HttpServletResponse response,HttpServletRequest request){
		return "/basic/log/index";
	}

	/**
	 * 返回编辑界面log_form
	 */
	@Hidden
	@GetMapping("/form")
	@RequiresPermissions("basic:log:view")
	public String form(@ModelAttribute LogEntity log,HttpServletResponse response,HttpServletRequest request,ModelMap model){
		return "/basic/log/form";
	}

	/**
	 * 查询系统日志列表
	 * @param log 系统日志实体
	 */
	@Operation(summary =  "查询系统日志列表接口")
	@Parameters({
    	@Parameter(name = "logTitle", description = "标题", required = false, in = ParameterIn.QUERY),
    	@Parameter(name = "logIp", description = "IP", required = false, in = ParameterIn.QUERY),
    	@Parameter(name = "logMethod", description = "请求方法", required = false, in = ParameterIn.QUERY),
    	@Parameter(name = "logRequestMethod", description = "请求方式", required = false, in = ParameterIn.QUERY),
    	@Parameter(name = "logUrl", description = "请求地址", required = false, in = ParameterIn.QUERY),
    	@Parameter(name = "logStatus", description = "请求状态", required = false, in = ParameterIn.QUERY),
    	@Parameter(name = "logBusinessType", description = "业务类型", required = false, in = ParameterIn.QUERY),
    	@Parameter(name = "logUserType", description = "用户类型", required = false, in = ParameterIn.QUERY),
    	@Parameter(name = "logUser", description = "操作人员", required = false, in = ParameterIn.QUERY),
    	@Parameter(name = "logLocation", description = "所在地区", required = false, in = ParameterIn.QUERY),
    	@Parameter(name = "logParam", description = "请求参数", required = false, in = ParameterIn.QUERY),
    	@Parameter(name = "logResult", description = "返回参数", required = false, in = ParameterIn.QUERY),
    	@Parameter(name = "logErrorMsg", description = "错误消息", required = false, in = ParameterIn.QUERY),
    	@Parameter(name = "createBy", description = "创建人", required = false, in = ParameterIn.QUERY),
    	@Parameter(name = "createDate", description = "创建时间", required = false, in = ParameterIn.QUERY),
    	@Parameter(name = "updateBy", description = "修改人", required = false, in = ParameterIn.QUERY),
    	@Parameter(name = "updateDate", description = "修改时间", required = false, in = ParameterIn.QUERY),
    	@Parameter(name = "del", description = "删除标记", required = false, in = ParameterIn.QUERY),
    	@Parameter(name = "id", description = "编号", required = false, in = ParameterIn.QUERY),
    })
	@RequestMapping(value ="/list",method = {RequestMethod.GET,RequestMethod.POST})
	@ResponseBody
	@RequiresPermissions("basic:log:view")
	public ResultData list(@ModelAttribute @Parameter(hidden = true) LogBean log, HttpServletResponse response, HttpServletRequest request, @Parameter(hidden = true) ModelMap model, BindingResult result) {
		BasicUtil.startPage();
		List<LogEntity> logList = logBiz.query(log);
		return ResultData.build().success(new EUListBean(logList,(int)BasicUtil.endPage(logList).getTotal()));
	}

	/**
	 * 获取系统日志
	 * @param log 系统日志实体
	 */
	@Operation(summary =  "获取系统日志列表接口")
    @Parameter(name = "id", description = "编号", required = true, in = ParameterIn.QUERY)
	@GetMapping("/get")
	@ResponseBody
	@RequiresPermissions("basic:log:view")
	public ResultData get(@ModelAttribute @Parameter(hidden = true) LogEntity log,HttpServletResponse response, HttpServletRequest request,@Parameter(hidden = true) ModelMap model){
		if(log.getId()==null) {
			return ResultData.build().error("ID不能为空!");
		}
		LogEntity _log = logBiz.getById(log.getId());
		return ResultData.build().success(_log);
	}

	/**
	 * 获取日志类型枚举类
	 */
	@Operation(summary =  "获取日志类型枚举类")
	@PostMapping("/queryLogType")
	@ResponseBody
	public ResultData queryLogType(HttpServletResponse response, HttpServletRequest request) {
		List<HashMap<String, String>> list = new ArrayList<>();
		try {
			Class _class = Class.forName(BusinessTypeEnum.class.getName());
			Object[] enumConstants = _class.getEnumConstants();
			Arrays.stream(enumConstants).forEach(e-> {
				HashMap<String, String> map = new HashMap<>();
				map.put("value", e.toString().toLowerCase());
				try {
					Method label = e.getClass().getMethod("getLabel");
					map.put("label", label.invoke(e).toString());
				} catch (NoSuchMethodException ex) {
					ex.printStackTrace();
				} catch (IllegalAccessException ex) {
					ex.printStackTrace();
				} catch (InvocationTargetException ex) {
					ex.printStackTrace();
				}
				list.add(map);
			});
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return ResultData.build().success(list);
	}
}
