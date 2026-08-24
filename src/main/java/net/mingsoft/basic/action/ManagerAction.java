









package net.mingsoft.basic.action;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
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
import net.mingsoft.basic.bean.EUListBean;
import net.mingsoft.basic.biz.IManagerBiz;
import net.mingsoft.basic.constant.e.BusinessTypeEnum;
import net.mingsoft.basic.constant.e.ManagerAdminEnum;
import net.mingsoft.basic.entity.AppEntity;
import net.mingsoft.basic.entity.ManagerEntity;
import net.mingsoft.basic.util.BasicUtil;
import net.mingsoft.basic.util.StringUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 管理员管理控制层
 * @version
 * 版本号：1.0<br/>
 * 创建日期：2017-8-24 23:40:55<br/>
 * 历史修订： 2022-1-27 12:00 list(), query() 添加站群插件查询管理员的容错 <br/>
 */
@Tag(name = "后端-基础接口")
@Controller
@RequestMapping("/${ms.manager.path}/basic/manager")
public class ManagerAction extends BaseAction{

	/**
	 * 注入管理员业务层
	 */
	@Autowired
	private IManagerBiz managerBiz;

	/**
	 * 返回主界面index
	 */
	@Hidden
	@GetMapping("/index")
	@RequiresPermissions("basic:manager:view")
	public String index(HttpServletResponse response, HttpServletRequest request){
		return "/basic/manager/index";
	}


	@Operation(summary = "查询管理员列表")
	@Parameters({
			@Parameter(name = "managerName", description = "账号", required = false, in = ParameterIn.QUERY),
			@Parameter(name = "managerNickName", description = "昵称", required = false, in = ParameterIn.QUERY)
	})
	@GetMapping("/list")
	@RequiresPermissions("basic:manager:view")
	@ResponseBody
	public ResultData list(@ModelAttribute @Parameter(hidden = true) ManagerEntity manager,HttpServletResponse response, HttpServletRequest request,@Parameter(hidden = true) ModelMap model) {
		BasicUtil.startPage();
		AppEntity websiteApp = BasicUtil.getWebsiteApp();
		List<ManagerEntity> managerList;
		if (websiteApp != null){
			String appId = websiteApp.getAppId();
			LambdaQueryWrapper<ManagerEntity> wrapper = new LambdaQueryWrapper<>();
			wrapper.like(StringUtils.isNotBlank(manager.getManagerName()), ManagerEntity::getManagerName, manager.getManagerName());
			wrapper.like(StringUtils.isNotBlank(manager.getManagerNickName()), ManagerEntity::getManagerNickName, manager.getManagerNickName());
			wrapper.eq(StringUtils.isNotBlank(manager.getRoleIds()),ManagerEntity::getRoleIds,manager.getRoleIds());
			wrapper.apply("APP_ID={0}",appId);
			managerList = managerBiz.list(wrapper);
		}else {
			managerList = managerBiz.query(manager);
		}
		List<ManagerEntity> allManager = managerBiz.queryAllManager(managerList);
		return ResultData.build().success( BasicUtil.filter(new EUListBean(allManager, (int) BasicUtil.endPage(allManager).getTotal()), "managerPassword",
				"updateBy",
				"createBy",
				"del"));

	}


	/**
	 * 处理creatBy等需要关联查询管理员表的场景，后端组织管理员信息，统一在页面中格式化
	 * @return [{id(管理员id),managerNickName(管理员昵称)}]
	 */
	@Operation(summary = "查询管理员名称列表")
	@GetMapping("/queryManager")
	@ResponseBody
	public ResultData queryManager(HttpServletResponse response, HttpServletRequest request) {
		AppEntity websiteApp = BasicUtil.getWebsiteApp();
		LambdaQueryWrapper<ManagerEntity> wrapper = new LambdaQueryWrapper<>();
		wrapper.select(ManagerEntity::getId,ManagerEntity::getManagerNickName);
		if(websiteApp!=null){
			wrapper.apply("APP_ID={0}",websiteApp.getAppId());
		}
		return ResultData.build().success(managerBiz.list(wrapper).stream()
				.map(managerEntity -> Map.of(
						"id", managerEntity.getId(),
						"managerNickName", managerEntity.getManagerNickName()
				)).collect(Collectors.toList()));
	}


	@Operation(summary =  "查询管理员列表,去掉当前管理员id，确保不能删除和修改自己")
	@GetMapping("/query")
	@RequiresPermissions("basic:manager:view")
	@ResponseBody
	public ResultData query(HttpServletResponse response, HttpServletRequest request, @Parameter(hidden = true) ModelMap model) {
		ManagerEntity manager = BasicUtil.getManager();
		BasicUtil.startPage();
		AppEntity websiteApp = BasicUtil.getWebsiteApp();
		List<ManagerEntity> managerList;
		if (websiteApp != null){
			String appId = websiteApp.getAppId();
			QueryWrapper<ManagerEntity> wrapper = new QueryWrapper<ManagerEntity>().eq("APP_ID", appId);
			managerList = managerBiz.list(wrapper);
		}else {
			managerList = managerBiz.list();

		}
		List<ManagerEntity> allManager = managerBiz.queryAllManager(managerList);
		for (ManagerEntity _manager : allManager) {
			assert manager != null;
			if (_manager.getId().equals(manager.getId())) {
				_manager.setId("0");
			}
		}
		return ResultData.build().success(new EUListBean(allManager, (int) BasicUtil.endPage(allManager).getTotal()));

	}

	@Operation(summary = "获取管理员接口")
	@Parameter(name = "id", description = "管理员id", required =  false, in = ParameterIn.QUERY)
	@GetMapping("/get")
	@RequiresPermissions("basic:manager:view")
	@ResponseBody
	public ResultData get(@ModelAttribute @Parameter(hidden = true) ManagerEntity manager,HttpServletResponse response, HttpServletRequest request,@Parameter(hidden = true) ModelMap model){
		ManagerEntity managerEntity;
		//判断是否传managerId
		if (StringUtils.isNotEmpty(manager.getId())) {
			managerEntity = managerBiz.getById(manager.getId());
		} else {
			ManagerEntity managerSession = BasicUtil.getManager();
			if (managerSession == null) {
				return ResultData.build().error("管理员已失效!");
			}
			managerEntity = managerBiz.getById(managerSession.getId());
		}
		if (managerEntity != null){
			managerEntity.setManagerPassword("");
		}
		return ResultData.build().success(managerEntity);
	}

	@Operation(summary = "获取当前管理员信息接口")
	@GetMapping("/info")
	@ResponseBody
	public ResultData info(HttpServletResponse response, HttpServletRequest request){
		ManagerEntity managerEntity =  BasicUtil.getManager();
		if (managerEntity == null) {
			return ResultData.build().error("管理员已失效!");
		}
		managerEntity = managerBiz.getById(managerEntity.getId());
		if (managerEntity != null){
			managerEntity.setManagerPassword("");
		}
		Map<String, Object> stringObjectMap = BeanUtil.beanToMap(managerEntity);
		stringObjectMap.put("sessionId", SecurityUtils.getSubject().getSession().getId());
		return ResultData.build().success(stringObjectMap);
	}


	@Operation(summary =  "保存管理员实体")
	@Parameters({
			@Parameter(name = "managerName", description = "帐号", required =  true, in = ParameterIn.QUERY),
			@Parameter(name = "managerNickName", description = "昵称", required =  true, in = ParameterIn.QUERY),
			@Parameter(name = "managerPassword", description = "密码", required =  true, in = ParameterIn.QUERY),
			@Parameter(name = "roleIds", description = "角色IDS", required =  true, in = ParameterIn.QUERY),
	})
	@LogAnn(title = "保存管理员实体",businessType= BusinessTypeEnum.INSERT)
	@PostMapping("/save")
	@ResponseBody
	@RequiresPermissions("basic:manager:save")
	public ResultData save(@ModelAttribute @Parameter(hidden = true) ManagerEntity manager, HttpServletResponse response, HttpServletRequest request) {
		//验证管理员用户名的值是否合法
		if(StringUtils.isBlank(manager.getManagerName())){
			return ResultData.build().error(getResString("err.empty", this.getResString("manager.name")));
		}
		if(!StringUtil.checkLength(manager.getManagerName()+"", 6, 15)){
			return ResultData.build().error(getResString("err.length", this.getResString("manager.name"), "6", "15"));
		}
		//用户名是否存在
		if(managerBiz.getManagerByManagerName(manager.getManagerName())!= null){
			return ResultData.build().error(getResString("err.exist", this.getResString("manager.name")));
		}
		if (!manager.getManagerName().matches("^[a-zA-Z0-9_]{6,15}$")) {
			return ResultData.build().error(getResString("err.error", this.getResString("manager.name")));
		}
		//新增时不允许设置管理员标识,为了方便apifox响应校验抛出异常
		if (StringUtils.isNotBlank(manager.getManagerAdmin())){
			return ResultData.build().error(getResString("err.error", this.getResString("manager")));
		}
		// 新增不允许设置管理员标识
		manager.setManagerAdmin(null);
		//验证管理员昵称的值是否合法
		if(StringUtils.isBlank(manager.getManagerNickName())){
			return ResultData.build().error(getResString("err.empty", this.getResString("manager.nickname")));
		}
		if(!StringUtil.checkLength(manager.getManagerNickName()+"", 1, 15)){
			return ResultData.build().error(getResString("err.length", this.getResString("manager.nickname"), "1", "15"));
		}
        //验证roleIds的值是否合法
        if(StringUtils.isBlank(manager.getRoleIds())){
            return ResultData.build().error(getResString("err.empty", this.getResString("manager.roleid")));
        }
		if(!StringUtil.checkLength(manager.getRoleIds()+"", 1, 50)){
			return ResultData.build().error(getResString("err.length", this.getResString("manager.roleid"), "1", "50"));
		}
		//验证管理员密码的值是否合法
		if(StringUtils.isBlank(manager.getManagerPassword())){
			return ResultData.build().error(getResString("err.empty", this.getResString("manager.password")));
		}
		if(!StringUtil.checkLength(manager.getManagerPassword()+"", 6, 30)){
			return ResultData.build().error(getResString("err.length", this.getResString("manager.password"), "6", "30"));
		}
		if (!manager.getManagerPassword().matches("(?!^(\\d+|[a-zA-Z]+|[~!@#$%^&*?]+)$)^[\\w~!@#$%^&*?]{6,30}$")) {
			return ResultData.build().error(getResString("err.error", this.getResString("manager.password")));
		}

		manager.setManagerPassword(SecureUtil.md5(manager.getManagerPassword()));
		managerBiz.save(manager);
		managerBiz.updateCache();
		return ResultData.build().success(manager);
	}


	@Operation(summary =  "批量删除管理员")
	@LogAnn(title = "批量删除管理员",businessType= BusinessTypeEnum.DELETE)
	@PostMapping("/delete")
	@ResponseBody
	@RequiresPermissions("basic:manager:del")
	public ResultData delete(@RequestBody List<ManagerEntity> managers,HttpServletResponse response, HttpServletRequest request) {
		// 查询自己Id,不允许删除自己
		ManagerEntity manager = BasicUtil.getManager();
		Integer[] ids = new Integer[managers.size()];
		for(int i = 0;i<managers.size();i++){
			ids[i] = Integer.parseInt(managers.get(i).getId());
		}
		// 先查出需要删除id的信息
		List<ManagerEntity> managerEntities = managerBiz.listByIds(Arrays.asList(ids));
		managerEntities = managerEntities.stream().filter(managerEntity -> {
			return ManagerAdminEnum.SUPER.toString().equals(managerEntity.getManagerAdmin()) || ManagerAdminEnum.SUPERADMIN.toString().equals(managerEntity.getManagerAdmin()) || manager.getId().equals(managerEntity.getId());
		}).collect(Collectors.toList());
		if (CollectionUtil.isNotEmpty(managerEntities)) {
			LOG.error("非法操作删除超管账号或自己账号");
			return ResultData.build().error(getResString("fail", getResString("remove")));
		}
		managerBiz.removeByIds(Arrays.asList(ids));
		managerBiz.updateCache();
		return ResultData.build().success();
	}

	@Operation(summary =  "更新管理员信息")
	@Parameters({
			@Parameter(name = "id", description = "id", required =  true, in = ParameterIn.QUERY),
			@Parameter(name = "managerNickName", description = "昵称", required =  true, in = ParameterIn.QUERY),
			@Parameter(name = "managerPassword", description = "密码", required =  false, in = ParameterIn.QUERY),
			@Parameter(name = "roleIds", description = "角色IDS", required =  true, in = ParameterIn.QUERY),
	})
	@LogAnn(title = "更新管理员信息",businessType= BusinessTypeEnum.UPDATE)
	@PostMapping("/update")
	@ResponseBody
	@RequiresPermissions("basic:manager:update")
	public ResultData update(@ModelAttribute @Parameter(hidden = true) ManagerEntity manager) {
		managerBiz.updateCache();

		ManagerEntity _manager = managerBiz.getById(manager.getId());
		if (_manager == null) {
			return ResultData.build().error(getResString("err.not.exist", getResString("managerName")));
		}
		manager.setManagerAdmin(null);
		//验证管理员昵称的值是否合法
		if(StringUtils.isBlank(manager.getManagerNickName())){
			return ResultData.build().error(getResString("err.empty", this.getResString("manager.nickname")));
		}
		if(!StringUtil.checkLength(manager.getManagerNickName()+"", 1, 15)){
			return ResultData.build().error(getResString("err.length", this.getResString("manager.nickname"), "1", "15"));
		}
		//验证roleIds的值是否合法
		if(StringUtils.isBlank(manager.getRoleIds())){
			return ResultData.build().error(getResString("err.empty", this.getResString("manager.roleid")));
		}
		if(!StringUtil.checkLength(manager.getRoleIds()+"", 1, 50)){
			return ResultData.build().error(getResString("err.length", this.getResString("manager.roleid"), "1", "50"));
		}
		//验证管理员密码的值是否合法
		if(!StringUtils.isBlank(manager.getManagerPassword())){
			if(!StringUtil.checkLength(manager.getManagerPassword()+"", 6, 30)){
				return ResultData.build().error(getResString("err.length", this.getResString("manager.password"), "6", "30"));
			}
			if (!manager.getManagerPassword().matches("(?!^(\\d+|[a-zA-Z]+|[~!@#$%^&*?]+)$)^[\\w~!@#$%^&*?]{6,30}$")) {
				return ResultData.build().error(getResString("err.error", this.getResString("manager.password")));
			}
			manager.setManagerPassword(SecureUtil.md5(manager.getManagerPassword()));
		} else {
			manager.setManagerPassword(null);
		}
		//不允许修改管理员账号,将它设置为和原名称一样
		manager.setManagerName(_manager.getManagerName());
		managerBiz.updateById(manager);
		return ResultData.build().success(manager);
	}
}
