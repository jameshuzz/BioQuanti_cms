package net.mingsoft.cms.action;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import net.mingsoft.base.entity.ResultData;
import net.mingsoft.basic.annotation.LogAnn;
import net.mingsoft.basic.constant.e.BusinessTypeEnum;
import net.mingsoft.cms.biz.IIntroTplBiz;
import net.mingsoft.cms.entity.IntroTplEntity;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

/**
 * 产品简介模板管理控制层
 * 模板CRUD + 编辑页按栏目取绑定骨架
 * @version
 * 版本号：1.0.0<br/>
 * 创建日期：2026-09-18<br/>
 */
@Tag(name = "后端-产品简介模板接口")
@Controller("cmsIntroTplAction")
@RequestMapping("/${ms.manager.path}/cms/introtpl")
public class IntroTplAction extends BaseAction {

	@Autowired
	private IIntroTplBiz introTplBiz;

	/**
	 * 返回主界面index
	 */
	@GetMapping("/index")
	public String index() {
		return "/cms/introtpl/index";
	}

	/**
	 * 模板列表
	 */
	@Operation(summary = "模板列表")
	@GetMapping("/list")
	@ResponseBody
	@RequiresPermissions("cms:introtpl:view")
	public ResultData list() {
		return ResultData.build().success(introTplBiz.list());
	}

	/**
	 * 按栏目取绑定模板（文章编辑页注入用，仅登录即可）
	 */
	@Operation(summary = "按栏目取模板")
	@GetMapping("/getByCategory")
	@ResponseBody
	public ResultData getByCategory(@RequestParam(required = false) String categoryId,
	                                @RequestParam(required = false) String linkId) {
		IntroTplEntity tpl = introTplBiz.getByCategory(categoryId, linkId);
		return ResultData.build().success(tpl);
	}

	/**
	 * 保存/更新模板
	 */
	@Operation(summary = "保存模板")
	@PostMapping("/save")
	@ResponseBody
	@LogAnn(title = "保存产品简介模板", businessType = BusinessTypeEnum.UPDATE)
	@RequiresPermissions("cms:introtpl:view")
	public ResultData save(@RequestParam String tplName,
	                       @RequestParam String categoryIds,
	                       @RequestParam(required = false) String rows,
	                       @RequestParam(required = false) String id,
	                       @RequestParam(required = false) String remark) {
		IntroTplEntity entity = new IntroTplEntity();
		entity.setId(id);
		entity.setTplName(tplName);
		entity.setCategoryIds(categoryIds);
		entity.setRemark(remark);
		introTplBiz.saveTpl(entity, rows);
		return ResultData.build().success(entity);
	}

	/**
	 * 删除模板
	 */
	@Operation(summary = "删除模板")
	@PostMapping("/delete")
	@ResponseBody
	@LogAnn(title = "删除产品简介模板", businessType = BusinessTypeEnum.DELETE)
	@RequiresPermissions("cms:introtpl:view")
	public ResultData delete(@RequestParam String id) {
		if (StringUtils.isBlank(id)) {
			return ResultData.build().error("id不能为空");
		}
		introTplBiz.removeById(id);
		return ResultData.build().success();
	}
}
