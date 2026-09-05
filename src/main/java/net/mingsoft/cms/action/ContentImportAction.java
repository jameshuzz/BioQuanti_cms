package net.mingsoft.cms.action;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import net.mingsoft.base.entity.ResultData;
import net.mingsoft.basic.annotation.LogAnn;
import net.mingsoft.basic.constant.e.BusinessTypeEnum;
import net.mingsoft.cms.biz.IContentImportBiz;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * 文章Excel导入控制层
 * 模板下载（含栏目下拉）+ 批量导入
 * @version
 * 版本号：1.0.0<br/>
 * 创建日期：2026-09-02<br/>
 */
@Tag(name = "后端-文章Excel导入接口")
@Controller("cmsContentImportAction")
@RequestMapping("/${ms.manager.path}/cms/content/import")
public class ContentImportAction extends BaseAction {

	@Autowired
	private IContentImportBiz contentImportBiz;

	/**
	 * 下载导入模板（Excel含栏目下拉选择，示例行导入前删除）
	 */
	@Operation(summary = "下载文章导入模板")
	@GetMapping("/template")
	@ResponseBody
	@RequiresPermissions("cms:content:save")
	public void template(HttpServletResponse response) {
		contentImportBiz.downloadTemplate(response);
	}

	/**
	 * 上传Excel批量导入：按栏目名建文章，按货号匹配更新（已有货号=更新文章+规格数据）
	 *
	 * @param file           .xlsx文件
	 * @param generateStatic 导入成功后是否对涉及栏目重新静态化（列表页+详情页）
	 */
	@Operation(summary = "Excel导入文章")
	@PostMapping
	@ResponseBody
	@LogAnn(title = "Excel导入文章", businessType = BusinessTypeEnum.INSERT)
	@RequiresPermissions("cms:content:save")
	public ResultData importExcel(@RequestParam("file") MultipartFile file,
								  @RequestParam(value = "generateStatic", required = false, defaultValue = "false") boolean generateStatic) {
		if (file == null || file.isEmpty()) {
			return ResultData.build().error(getResString("err.empty", "文件"));
		}
		String name = file.getOriginalFilename();
		if (name == null || !name.toLowerCase().endsWith(".xlsx")) {
			return ResultData.build().error("仅支持.xlsx格式文件");
		}
		Map<String, Object> result = contentImportBiz.importExcel(file, generateStatic);
		return ResultData.build().success(result);
	}
}
