







package net.mingsoft.basic.action;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import net.mingsoft.base.entity.ResultData;
import net.mingsoft.basic.annotation.LogAnn;
import net.mingsoft.basic.bean.EUListBean;
import net.mingsoft.basic.bean.RoleBean;
import net.mingsoft.basic.biz.IModelBiz;
import net.mingsoft.basic.biz.IRoleBiz;
import net.mingsoft.basic.biz.IRoleModelBiz;
import net.mingsoft.basic.constant.e.BusinessTypeEnum;
import net.mingsoft.basic.entity.ManagerEntity;
import net.mingsoft.basic.entity.RoleEntity;
import net.mingsoft.basic.entity.RoleModelEntity;
import net.mingsoft.basic.util.BasicUtil;
import net.mingsoft.basic.util.StringUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 角色管理控制层
 * @version
 * 版本号：1.0<br/>
 * 创建日期：2017-8-24 23:40:55<br/>
 * 历史修订：<br/>
 */
@Tag(name = "后端-基础接口")
@Controller
@RequestMapping("/${ms.manager.path}/basic/role")
public class RoleAction extends BaseAction{

	/**
	 * 注入角色业务层
	 */
	@Autowired
	private IRoleBiz roleBiz;
	/**
	 * 模块业务层
	 */
	@Autowired
	private IModelBiz modelBiz;
	/**
	 * 角色模块关联业务层
	 */
	@Autowired
	private IRoleModelBiz roleModelBiz;

	/**
	 * 返回主界面index
	 */
	@Hidden
	@GetMapping("/index")
	@RequiresPermissions("basic:role:view")
	public String index(HttpServletResponse response,HttpServletRequest request){
		return "/basic/role/index";
	}

	/**
	 * 返回编辑界面role_form
	 */
	@Hidden
	@GetMapping("/form")
	@RequiresPermissions("basic:role:view")
	public String form(@ModelAttribute @Parameter(hidden = true) RoleEntity role, HttpServletResponse response, HttpServletRequest request,@Parameter(hidden = true) ModelMap model){
		return "/basic/role/form";
	}

	@Operation(summary =  "查询角色列表")
	@Parameters({
		@Parameter(name = "roleName", description = "角色名称", required =  false, in = ParameterIn.QUERY)
	})
	@GetMapping("/list")
	//@RequiresPermissions("role:view") 此处权限在栏目权限管理等其他功能被调用，需放行
	@ResponseBody
	public ResultData list(@ModelAttribute @Parameter(hidden = true) RoleEntity role,HttpServletResponse response, HttpServletRequest request,@Parameter(hidden = true) ModelMap model) {
		BasicUtil.startPage();
		List roleList = roleBiz.query(role);
		return ResultData.build().success(new EUListBean(roleList,(int)BasicUtil.endPage(roleList).getTotal()));
	}

	@Operation(summary =  "根据角色ID查询模块集合")
	@Parameter(name = "roleId", description = "角色ID", required = true, in = ParameterIn.PATH)
	@GetMapping("/{roleId}/queryByRole")
	@ResponseBody
	public ResultData queryByRole(@PathVariable @Parameter(hidden = true) int roleId, HttpServletResponse response){
		List models = modelBiz.queryModelByRoleId(roleId);
		return ResultData.build().success(models);
	}

	@Operation(summary =  "查询所有角色列表")
	@Parameters({
			@Parameter(name = "roleName", description = "角色名称", required =  false, in = ParameterIn.QUERY)
	})
	@GetMapping("/all")
	@ResponseBody
	public ResultData all(@ModelAttribute @Parameter(hidden = true) RoleEntity role,HttpServletResponse response, HttpServletRequest request,@Parameter(hidden = true) ModelMap model) {
		BasicUtil.startPage();
		List roleList = roleBiz.query(role);
		return ResultData.build().success(new EUListBean(roleList,(int)BasicUtil.endPage(roleList).getTotal()));
	}

	@Operation(summary =  "获取角色")
	@Parameter(name = "id", description = "角色ID", required =  true, in = ParameterIn.QUERY)
	@GetMapping("/get")
	@RequiresPermissions("basic:role:view")
	@ResponseBody
	public ResultData get(@ModelAttribute @Parameter(hidden = true) RoleEntity role,HttpServletResponse response, HttpServletRequest request,@Parameter(hidden = true) ModelMap model){
		if(StringUtils.isEmpty(role.getId())) {
			return ResultData.build().error(getResString("err.error", this.getResString("role.id")));
		}
		RoleEntity _role = (RoleEntity)roleBiz.getEntity(Integer.parseInt(role.getId()));
		return ResultData.build().success(_role);
	}


	@Operation(summary =  "保存角色实体")
	@Parameters({
		@Parameter(name = "roleName", description = "角色名称", required =  true, in = ParameterIn.QUERY),
		@Parameter(name = "ids", description = "功能权限id", required =  true, in = ParameterIn.QUERY),
	})
	@LogAnn(title = "保存角色实体",businessType= BusinessTypeEnum.UPDATE)
	@PostMapping("/saveOrUpdateRole")
	@ResponseBody
	@RequiresPermissions(value = {"basic:role:save","basic:role:update"}, logical = Logical.OR)
	public ResultData saveOrUpdateRole(@ModelAttribute @Parameter(hidden = true) RoleBean role, HttpServletResponse response, HttpServletRequest request) {
		//组织角色属性，并对角色进行保存
		RoleBean _role = new RoleBean();
		_role.setRoleName(role.getRoleName());
		//获取管理员id
		if(StringUtils.isEmpty(role.getRoleName())){
			return ResultData.build().error(getResString("err.empty", this.getResString("roleName")));
		}
		if(!StringUtil.checkLength(role.getRoleName()+"", 1, 30)){
			return ResultData.build().error(getResString("err.length", this.getResString("role.name"), "1", "30"));
		}
		RoleBean roleBean = (RoleBean) roleBiz.getEntity(_role);
		//通过角色id判断是保存还是修改
		if(StringUtils.isNotEmpty(role.getId())){
			//若为更新角色，数据库中存在该角色名称且当前名称不为更改前的名称，则属于重名
			if(roleBean != null && !roleBean.getId().equals(role.getId())){
				return ResultData.build().error(getResString("roleName.exist"));
			}
			role.setUpdateBy(BasicUtil.getManager().getId());
			role.setUpdateDate(new Date());
			role.setCreateBy(null);
			roleBiz.updateById(role);
		}else{
			//判断角色名是否重复
			if(roleBean != null){
				return ResultData.build().error(getResString("roleName.exist"));
			}
			role.setCreateBy(BasicUtil.getManager().getId());
			role.setCreateDate(new Date());
			//获取管理员id
			roleBiz.save(role);
			roleBiz.updateCache();
		}
		//开始保存相应的关联数据。组织角色模块的列表。
		List<RoleModelEntity> roleModelList = new ArrayList<>();
		if(!StringUtils.isEmpty(role.getIds())){
			for(String id : role.getIds().split(",")){
				RoleModelEntity roleModel = new RoleModelEntity();
				roleModel.setRoleId(Integer.parseInt(role.getId()));
				roleModel.setModelId(Integer.parseInt(id));
				roleModelList.add(roleModel);
			}
			//先删除当前的角色关联菜单，然后重新添加。
			roleModelBiz.deleteByRoleId(Integer.parseInt(role.getId()));
			modelBiz.updateCache();

			//加上数量参数用于区分IBaseBiz的重名方法
			roleModelBiz.saveBatch(roleModelList, roleModelList.size());
		}else{
			roleModelBiz.deleteByRoleId(Integer.parseInt(role.getId()));
		}
		roleBiz.updateCache();
		return ResultData.build().success(role);
	}


	@Operation(summary =  "批量删除角色")
	@PostMapping("/delete")
	@ResponseBody
	@RequiresPermissions("basic:role:del")
	@LogAnn(title = "删除角色", businessType = BusinessTypeEnum.DELETE)
	public ResultData delete(@RequestBody List<RoleEntity> roles,HttpServletResponse response, HttpServletRequest request) {
		//获取当前登录管理员的所属角色信息
		ManagerEntity managerSession = BasicUtil.getManager();

		if (roleBiz.deleteRoleByRoles(roles,managerSession)){
			return ResultData.build().success("删除成功",null);
		}
		return ResultData.build().success("删除成功，已过滤当前不可删除角色",null);
	}
}
