package net.mingsoft.cms.action;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import net.mingsoft.base.entity.ResultData;
import net.mingsoft.basic.annotation.LogAnn;
import net.mingsoft.basic.constant.e.BusinessTypeEnum;
import net.mingsoft.base.exception.BusinessException;
import net.mingsoft.base.util.BundleUtil;
import net.mingsoft.cms.biz.IManualTemplateBiz;
import net.mingsoft.cms.entity.ManualTemplateEntity;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 说明书模板管理控制层
 * 模板上传/替换/绑定/预览/磁盘治理
 * @version
 * 版本号：1.0.0<br/>
 * 创建日期：2026-08-30<br/>
 */
@Tag(name = "后端-说明书模板管理接口")
@Controller("cmsManualAction")
@RequestMapping("/${ms.manager.path}/cms/manual")
public class ManualAction extends BaseAction {

	@Autowired
	private IManualTemplateBiz manualTemplateBiz;

	/**
	 * 返回主界面index
	 */
	@GetMapping("/index")
	public String index() {
		return "/cms/manual/index";
	}

	/**
	 * 模板列表（含绑定数统计）
	 */
	@Operation(summary = "模板列表")
	@RequestMapping(value = "/list", method = {RequestMethod.GET, RequestMethod.POST})
	@ResponseBody
	@RequiresPermissions("cms:manual:view")
	public ResultData list() {
		return ResultData.build().success(manualTemplateBiz.listWithCount());
	}

	/**
	 * 一键生成附件：对该模板已绑定的全部产品生成说明书PDF附件（回填MANUAL字段）并定向静态化产品页面
	 */
	@Operation(summary = "一键生成说明书附件")
	@PostMapping("/attach/generate")
	@ResponseBody
	@LogAnn(title = "一键生成说明书附件", businessType = BusinessTypeEnum.UPDATE)
	@RequiresPermissions("cms:manual:bind")
	public ResultData generateAttach(@RequestParam String templateId) {
		Map<String, Object> result = manualTemplateBiz.generateAttachments(templateId);
		// 失败明细放msg，成功数放data
		List<String> errors = (List<String>) result.get("errors");
		if (errors != null && !errors.isEmpty()) {
			StringBuilder msg = new StringBuilder("共" + result.get("total") + "个产品，成功" + result.get("success")
					+ "个，失败" + errors.size() + "个：");
			for (int i = 0; i < errors.size() && i < 10; i++) {
				msg.append("[").append(errors.get(i)).append("]");
			}
			if (errors.size() > 10) {
				msg.append("等");
			}
			return ResultData.build().error(msg.toString());
		}
		return ResultData.build().success(result);
	}

	/**
	 * 新建模板
	 */
	@Operation(summary = "新建模板")
	@PostMapping("/save")
	@ResponseBody
	@LogAnn(title = "新建说明书模板", businessType = BusinessTypeEnum.INSERT)
	@RequiresPermissions("cms:manual:template")
	public ResultData save(@RequestParam("file") MultipartFile file,
	                       @RequestParam String templateName,
	                       @RequestParam(defaultValue = "cn") String templateLang,
	                       @RequestParam(required = false) String remark) {
		ManualTemplateEntity entity = manualTemplateBiz.saveTemplate(file, templateName, templateLang, remark);
		return ResultData.build().success(entity);
	}

	/**
	 * 更新模板（名称/备注/语言，可选替换文件）
	 */
	@Operation(summary = "更新模板")
	@PostMapping("/update")
	@ResponseBody
	@LogAnn(title = "更新说明书模板", businessType = BusinessTypeEnum.UPDATE)
	@RequiresPermissions("cms:manual:template")
	public ResultData update(@RequestParam String id,
	                         @RequestParam(required = false) String templateName,
	                         @RequestParam(required = false) String templateLang,
	                         @RequestParam(required = false) String remark,
	                         @RequestParam(value = "file", required = false) MultipartFile file) {
		if (file != null && !file.isEmpty()) {
			// 替换模板文件（删旧文件，绑定产品下次下载即用新模板）
			ManualTemplateEntity entity = manualTemplateBiz.replaceTemplateFile(id, file);
			if (StringUtils.isNotBlank(templateName)) {
				entity.setTemplateName(templateName);
			}
			if (StringUtils.isNotBlank(remark)) {
				entity.setRemark(remark);
			}
			if (StringUtils.isNotBlank(templateLang)) {
				entity.setTemplateLang("en".equalsIgnoreCase(templateLang) ? "en" : "cn");
			}
			return ResultData.build().success(entity);
		}
		return ResultData.build().error("请选择要替换的模板文件或使用基本信息编辑");
	}

	/**
	 * 删除模板（绑定数为0才允许）
	 */
	@Operation(summary = "删除模板")
	@PostMapping("/delete")
	@ResponseBody
	@LogAnn(title = "删除说明书模板", businessType = BusinessTypeEnum.DELETE)
	@RequiresPermissions("cms:manual:template")
	public ResultData delete(@RequestParam String id) {
		manualTemplateBiz.deleteTemplate(id);
		return ResultData.build().success();
	}

	/**
	 * 启用/停用模板
	 */
	@Operation(summary = "启用停用模板")
	@PostMapping("/enable")
	@ResponseBody
	@LogAnn(title = "启用停用说明书模板", businessType = BusinessTypeEnum.UPDATE)
	@RequiresPermissions("cms:manual:template")
	public ResultData enable(@RequestParam String id, @RequestParam String status) {
		ManualTemplateEntity entity = manualTemplateBiz.getById(id);
		if (entity == null) {
			return ResultData.build().error("模板不存在");
		}
		entity.setStatus("1".equals(status) ? "1" : "0");
		manualTemplateBiz.updateById(entity);
		return ResultData.build().success();
	}

	/**
	 * 可用占位符字段清单（动态读取模型字段定义）
	 */
	@Operation(summary = "占位符字段清单")
	@GetMapping("/fields")
	@ResponseBody
	@RequiresPermissions("cms:manual:view")
	public ResultData fields() {
		return ResultData.build().success(manualTemplateBiz.getFieldList());
	}

	/**
	 * 模板预览：原始模板转PDF（占位符{{X}}原样显示，无需绑定产品）
	 */
	@Operation(summary = "模板预览")
	@GetMapping("/preview")
	@RequiresPermissions("cms:manual:view")
	public void preview(@RequestParam String templateId, HttpServletResponse response) {
		Map<String, Object> result = manualTemplateBiz.previewTemplate(templateId);
		writePdf(response, result, true);
	}

	/**
	 * 读取模板HTML内容（在线编辑）
	 */
	@Operation(summary = "读取模板内容")
	@GetMapping("/content")
	@ResponseBody
	@RequiresPermissions("cms:manual:view")
	public ResultData getContent(@RequestParam String id) {
		return ResultData.build().success(manualTemplateBiz.getTemplateContent(id));
	}

	/**
	 * 下载模板HTML源文件
	 */
	@Operation(summary = "下载模板文件")
	@GetMapping("/download")
	@RequiresPermissions("cms:manual:view")
	public void download(@RequestParam String templateId, HttpServletResponse response) {
		Map<String, Object> result = manualTemplateBiz.downloadTemplate(templateId);
		byte[] bytes = (byte[]) result.get("bytes");
		String fileName = String.valueOf(result.get("fileName"));
		try {
			response.reset();
			response.setContentType("text/html;charset=UTF-8");
			response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''"
					+ URLEncoder.encode(fileName, StandardCharsets.UTF_8));
			response.setContentLength(bytes.length);
			try (OutputStream os = response.getOutputStream()) {
				os.write(bytes);
				os.flush();
			}
		} catch (Exception e) {
			throw new BusinessException(BundleUtil.getBaseString("err.error", new String[]{e.getMessage()}));
		}
	}

	/**
	 * 保存在线编辑的模板内容（干跑校验后覆写文件，绑定产品下次下载即生效）
	 */
	@Operation(summary = "保存模板内容")
	@PostMapping("/content/save")
	@ResponseBody
	@LogAnn(title = "在线编辑说明书模板", businessType = BusinessTypeEnum.UPDATE)
	@RequiresPermissions("cms:manual:template")
	public ResultData saveContent(@RequestParam String id, @RequestParam String content) {
		manualTemplateBiz.updateTemplateContent(id, content);
		return ResultData.build().success();
	}

	/**
	 * 绑定页产品列表
	 */
	@Operation(summary = "绑定页产品列表")
	@GetMapping("/bind/list")
	@ResponseBody
	@RequiresPermissions("cms:manual:bind")
	public ResultData bindList(@RequestParam(required = false) String templateId,
	                           @RequestParam(required = false) String categoryId,
	                           @RequestParam(required = false) String search,
	                           @RequestParam(required = false, defaultValue = "") String bindFilter,
	                           @RequestParam(defaultValue = "1") int page,
	                           @RequestParam(defaultValue = "20") int size) {
		return ResultData.build().success(manualTemplateBiz.queryProducts(categoryId, search, bindFilter, templateId, page, Math.min(size, 100)));
	}

	/**
	 * 批量保存绑定（一个产品只能绑一个模板）
	 * productIds：逗号分隔的文章id（ms.http为表单编码，数组以逗号串传递）
	 */
	@Operation(summary = "批量保存绑定")
	@PostMapping("/bind/save")
	@ResponseBody
	@LogAnn(title = "保存说明书模板绑定", businessType = BusinessTypeEnum.UPDATE)
	@RequiresPermissions("cms:manual:bind")
	public ResultData bindSave(@RequestParam String templateId, @RequestParam String productIds) {
		List<String> ids = Arrays.stream(productIds.split(","))
				.map(String::trim).filter(StringUtils::isNotBlank).collect(Collectors.toList());
		int rows = manualTemplateBiz.saveBind(templateId, ids);
		return ResultData.build().success(rows);
	}

	/**
	 * 磁盘扫描（孤儿模板文件检测）
	 */
	@Operation(summary = "磁盘扫描")
	@GetMapping("/disk/scan")
	@ResponseBody
	@RequiresPermissions("cms:manual:view")
	public ResultData diskScan() {
		return ResultData.build().success(manualTemplateBiz.diskScan());
	}

	/**
	 * 清理孤儿文件
	 */
	@Operation(summary = "清理孤儿文件")
	@PostMapping("/disk/clean")
	@ResponseBody
	@LogAnn(title = "清理说明书孤儿文件", businessType = BusinessTypeEnum.DELETE)
	@RequiresPermissions("cms:manual:template")
	public ResultData diskClean() {
		return ResultData.build().success(manualTemplateBiz.diskClean());
	}

	/**
	 * PDF输出（preview:预览=内联打开；下载=attachment）
	 */
	private void writePdf(HttpServletResponse response, Map<String, Object> result, boolean inline) {
		byte[] pdf = (byte[]) result.get("pdf");
		String fileName = String.valueOf(result.get("fileName"));
		try {
			response.reset();
			response.setContentType("application/pdf");
			response.setHeader("Content-Disposition", (inline ? "inline" : "attachment")
					+ "; filename*=UTF-8''" + URLEncoder.encode(fileName, StandardCharsets.UTF_8));
			response.setContentLength(pdf.length);
			try (OutputStream os = response.getOutputStream()) {
				os.write(pdf);
				os.flush();
			}
		} catch (Exception e) {
			throw new BusinessException(BundleUtil.getBaseString("err.error", new String[]{e.getMessage()}));
		}
	}
}
