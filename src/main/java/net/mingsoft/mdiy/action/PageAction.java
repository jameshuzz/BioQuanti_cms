



package net.mingsoft.mdiy.action;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import net.mingsoft.base.entity.BaseEntity;
import net.mingsoft.base.entity.ResultData;
import net.mingsoft.basic.annotation.LogAnn;
import net.mingsoft.basic.bean.EUListBean;
import net.mingsoft.basic.constant.e.BusinessTypeEnum;
import net.mingsoft.basic.util.BasicUtil;
import net.mingsoft.basic.util.StringUtil;
import net.mingsoft.mdiy.biz.IPageBiz;
import net.mingsoft.mdiy.entity.PageEntity;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 自定义页面表管理控制层
 * @version
 * 版本号：1<br/>
 * 创建日期：2017-8-11 14:01:54<br/>
 * 历史修订：<br/>
 */
@Tag(name = "后端-自定义模块接口")
@Controller
@RequestMapping("/${ms.manager.path}/mdiy/page")
public class PageAction extends BaseAction{

	/**
	 * 注入自定义页面表业务层
	 */
	@Autowired
	private IPageBiz pageBiz;

	/**
	 * 返回主界面index
	 */
	@Hidden
	@GetMapping("/index")
	public String index(HttpServletResponse response,HttpServletRequest request){
		return "/mdiy/page/index";
	}

	/**
	 * 返回编辑界面page_form
	 */
	@Hidden
	@GetMapping("/form")
	public String form(@ModelAttribute PageEntity page,HttpServletResponse response,HttpServletRequest request,@Parameter(hidden = true) ModelMap model){
		if(ObjectUtil.isNotEmpty(page) && StringUtils.isNotEmpty(page.getId())){
			BaseEntity pageEntity = pageBiz.getById(page.getId());
			model.addAttribute("pageEntity",pageEntity);
		}

		return "/mdiy/page/form";
	}

	/**
	 * 查询自定义页面表列表
	 * @param page 自定义页面表实体
	 * <i>page参数包含字段信息参考：</i><br/>
	 * pageAppId 应用id<br/>
	 * pagePath 自定义页面绑定模板的路径<br/>
	 * pageTitle 自定义页面标题<br/>
	 * pageKey 自定义页面访问路径<br/>
	 * <dt><span class="strong">返回</span></dt><br/>
	 * <dd>[<br/>
	 * { <br/>
	 * pageAppId: 应用id<br/>
	 * pagePath: 自定义页面绑定模板的路径<br/>
	 * pageTitle: 自定义页面标题<br/>
	 * pageKey: 自定义页面访问路径<br/>
	 * }<br/>
	 * ]</dd><br/>
	 */
	@Operation(summary =  "查询自定义页面列表接口")
	@Parameters({
    	@Parameter(name = "pagePath", description = "自定义页面绑定模板的路径", required =  false, in = ParameterIn.QUERY),
    	@Parameter(name = "pageTitle", description = "自定义页面标题", required =  false, in = ParameterIn.QUERY),
    	@Parameter(name = "pageKey", description = "自定义页面访问路径", required =  false, in = ParameterIn.QUERY)
    })
	@GetMapping("/list")
	@ResponseBody
	@RequiresPermissions("mdiy:page:view")
	public ResultData list(@ModelAttribute @Parameter(hidden = true) PageEntity page, HttpServletResponse response, HttpServletRequest request, @Parameter(hidden = true) ModelMap model) {
		BasicUtil.startPage();
		List pageList = pageBiz.query(page);
		return ResultData.build().success(new EUListBean(pageList,(int)BasicUtil.endPage(pageList).getTotal()));
	}

	/**
	 * 获取自定义页面表
	 * @param page 自定义页面表实体
	 * <i>page参数包含字段信息参考：</i><br/>
	 * pageAppId 应用id<br/>
	 * pagePath 自定义页面绑定模板的路径<br/>
	 * pageTitle 自定义页面标题<br/>
	 * pageKey 自定义页面访问路径<br/>
	 * <dt><span class="strong">返回</span></dt><br/>
	 * <dd>{ <br/>
	 * pageAppId: 应用id<br/>
	 * pagePath: 自定义页面绑定模板的路径<br/>
	 * pageTitle: 自定义页面标题<br/>
	 * pageKey: 自定义页面访问路径<br/>
	 * }</dd><br/>
	 */
	@Operation(summary =  "获取自定义页面接口")
	@Parameter(name = "id", description = "自定义页面编号", required =  true, in = ParameterIn.QUERY)
	@GetMapping("/get")
	@RequiresPermissions("mdiy:page:view")
	@ResponseBody
	public ResultData get(@ModelAttribute @Parameter(hidden = true) PageEntity page,HttpServletResponse response, HttpServletRequest request,@Parameter(hidden = true) ModelMap model){
		if(StringUtils.isEmpty(page.getId())){
			return ResultData.build().error( getResString("err.error", this.getResString("page.id")));
		}
		PageEntity _page = pageBiz.getById(page.getId());
		return ResultData.build().success(_page);
	}

	/**
	 * 保存自定义页面表实体
	 * @param page 自定义页面表实体
	 * <i>page参数包含字段信息参考：</i><br/>
	 * pageAppId 应用id<br/>
	 * pagePath 自定义页面绑定模板的路径<br/>
	 * pageTitle 自定义页面标题<br/>
	 * pageKey 自定义页面访问路径<br/>
	 * <dt><span class="strong">返回</span></dt><br/>
	 * <dd>{ <br/>
	 * pageAppId: 应用id<br/>
	 * pagePath: 自定义页面绑定模板的路径<br/>
	 * pageTitle: 自定义页面标题<br/>
	 * pageKey: 自定义页面访问路径<br/>
	 * }</dd><br/>
	 */
	@Operation(summary =  "保存自定义页面接口")
	@Parameters({
		@Parameter(name = "pagePath", description = "自定义页面绑定模板的路径", required =  true, in = ParameterIn.QUERY),
    	@Parameter(name = "pageTitle", description = "自定义页面标题", required =  true, in = ParameterIn.QUERY),
    	@Parameter(name = "pageKey", description = "自定义页面访问路径", required =  true, in = ParameterIn.QUERY),

    })
	@LogAnn(title = "保存自定义页面接口",businessType= BusinessTypeEnum.INSERT)
	@PostMapping("/save")
	@ResponseBody
	@RequiresPermissions("mdiy:page:save")
	public ResultData save(@ModelAttribute @Parameter(hidden = true) PageEntity page, HttpServletResponse response, HttpServletRequest request) {

		//验证自定义页面绑定模板的路径的值是否合法
		if(StringUtils.isBlank(page.getPagePath())){
			return ResultData.build().error(getResString("err.empty", this.getResString("page.path")));
		}
		if(!StringUtil.checkLength(page.getPagePath()+"", 1, 255)){
			return ResultData.build().error( getResString("err.length", this.getResString("page.path"), "1", "255"));
		}
		//验证自定义页面标题的值是否合法
		if(StringUtils.isBlank(page.getPageTitle())){
			return ResultData.build().error(getResString("err.empty", this.getResString("page.title")));
		}
		if(!StringUtil.checkLength(page.getPageTitle()+"", 1, 255)){
			return ResultData.build().error( getResString("err.length", this.getResString("page.title"), "1", "255"));
		}
		//验证自定义页面访问路径的值是否合法
		if(StringUtils.isBlank(page.getPageKey())){
			return ResultData.build().error(getResString("err.empty", this.getResString("page.key")));
		}
		if(!StringUtil.checkLength(page.getPageKey()+"", 1, 255)){
			return ResultData.build().error( getResString("err.length", this.getResString("page.key"), "1", "255"));
		}
		// 判断自定义页面访问路径的值是否唯一
		PageEntity pageEntity = new PageEntity();
		pageEntity.setPageKey(page.getPageKey());
		PageEntity pageByPageKey = pageBiz.getOne(new QueryWrapper<>(pageEntity), false);
		if (pageByPageKey != null){
			return ResultData.build().error(getResString("err.exist",this.getResString("page.key")));
		}
		//pageBiz.saveEntity(page); 过期
		pageBiz.save(page);
		return ResultData.build().success(page);
	}

	/**
	 * @param pages 自定义页面表实体
	 * <i>page参数包含字段信息参考：</i><br/>
	 * id:多个id直接用逗号隔开,例如id=1,2,3,4
	 * 批量删除自定义页面表
	 *            <dt><span class="strong">返回</span></dt><br/>
	 *            <dd>{code:"错误编码",<br/>
	 *            result:"true｜false",<br/>
	 *            resultMsg:"错误信息"<br/>
	 *            }</dd>
	 */
	@Operation(summary =  "批量删除自定义页面接口")
	@LogAnn(title = "批量删除自定义页面接口",businessType= BusinessTypeEnum.DELETE)
	@PostMapping("/delete")
	@ResponseBody
	@RequiresPermissions("mdiy:page:del")
	public ResultData delete(@RequestBody List<PageEntity> pages,HttpServletResponse response, HttpServletRequest request) {
		int[] ids = new int[pages.size()];
		for(int i = 0;i<pages.size();i++){
			ids[i] = Integer.parseInt(pages.get(i).getId());
		}
		pageBiz.delete(ids);
		return ResultData.build().success();
	}

	/**
	 * 更新自定义页面表信息自定义页面表
	 * @param page 自定义页面表实体
	 * <i>page参数包含字段信息参考：</i><br/>
	 * pageAppId 应用id<br/>
	 * pagePath 自定义页面绑定模板的路径<br/>
	 * pageTitle 自定义页面标题<br/>
	 * pageKey 自定义页面访问路径<br/>
	 * <dt><span class="strong">返回</span></dt><br/>
	 * <dd>{ <br/>
	 * pageAppId: 应用id<br/>
	 * pagePath: 自定义页面绑定模板的路径<br/>
	 * pageTitle: 自定义页面标题<br/>
	 * pageKey: 自定义页面访问路径<br/>
	 * }</dd><br/>
	 */
	@Operation(summary =  "更新自定义页面接口")
	@Parameters({
		@Parameter(name = "id", description = "自定义页面编号", required = true, in = ParameterIn.QUERY),
		@Parameter(name = "pagePath", description = "自定义页面绑定模板的路径", required =  true, in = ParameterIn.QUERY),
    	@Parameter(name = "pageTitle", description = "自定义页面标题", required =  true, in = ParameterIn.QUERY),
    	@Parameter(name = "pageKey", description = "自定义页面访问路径", required =  true, in = ParameterIn.QUERY),
    })
	@LogAnn(title = "更新自定义页面接口",businessType= BusinessTypeEnum.UPDATE)
	@PostMapping("/update")
	@ResponseBody
	@RequiresPermissions("mdiy:page:update")
	public ResultData update(@ModelAttribute @Parameter(hidden = true) PageEntity page, HttpServletResponse response,
			HttpServletRequest request) {
		//验证自定义页面绑定模板的路径的值是否合法
		if(StringUtils.isBlank(page.getPagePath())){
			return ResultData.build().error(getResString("err.empty", this.getResString("page.path")));
		}
		if(!StringUtil.checkLength(page.getPagePath()+"", 1, 255)){
			return ResultData.build().error( getResString("err.length", this.getResString("page.path"), "1", "255"));
		}
		//验证自定义页面标题的值是否合法
		if(StringUtils.isBlank(page.getPageTitle())){
			return ResultData.build().error(getResString("err.empty", this.getResString("page.title")));
		}
		if(!StringUtil.checkLength(page.getPageTitle()+"", 1, 255)){
			return ResultData.build().error( getResString("err.length", this.getResString("page.title"), "1", "255"));
		}
		//验证自定义页面访问路径的值是否合法
		if(StringUtils.isBlank(page.getPageKey())){
			return ResultData.build().error(getResString("err.empty", this.getResString("page.key")));
		}
		if(!StringUtil.checkLength(page.getPageKey()+"", 1, 255)){
			return ResultData.build().error( getResString("err.length", this.getResString("page.key"), "1", "255"));
		}
		// 更新时，判断自定义页面访问路径的值是否唯一
		PageEntity pageEntity = new PageEntity();
		pageEntity.setPageKey(page.getPageKey());
		PageEntity pageByPageKey = pageBiz.getOne(new QueryWrapper<>(pageEntity), false);
		if (pageByPageKey != null && !pageByPageKey.getId().equals(page.getId())){
			return ResultData.build().error(getResString("err.exist",this.getResString("page.key")));
		}

		pageBiz.updateById(page);
		return ResultData.build().success(page);
	}



/**
 * 校验参数*/
	@Operation(summary =  "校验参数接口")
	@GetMapping("/verify")
	@ResponseBody
	public ResultData verify(String fieldName, String fieldValue, String id, String idName){
		boolean verify = false;
		if(StringUtils.isBlank(id)){
			verify = super.validated("mdiy_page",fieldName,fieldValue);
		}else{
			verify = super.validated("mdiy_page",fieldName,fieldValue,id,idName);
		}
		if(verify){
			return ResultData.build().success(false);
		}else {
			return ResultData.build().success(true);
		}
	}

}
