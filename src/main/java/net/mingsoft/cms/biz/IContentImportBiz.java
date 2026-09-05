package net.mingsoft.cms.biz;

import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * 文章Excel导入业务层
 * 模板下载（含栏目下拉）+ 批量导入文章/产品规格数据
 * @version
 * 版本号：1.0.0<br/>
 * 创建日期：2026-09-02<br/>
 */
public interface IContentImportBiz {

	/**
	 * 下载导入模板：表头 = 文章字段 + 产品规格字段，栏目列带下拉选择（数据源为全部列表栏目）
	 *
	 * @param response 响应
	 */
	void downloadTemplate(HttpServletResponse response);

	/**
	 * 解析Excel并导入
	 *
	 * @param file           Excel文件（.xlsx）
	 * @param generateStatic 导入成功后是否对涉及栏目执行列表页+文章详情页静态化
	 * @return total/created/updated/failed/errors(逐行错误信息)/staticFailed
	 */
	Map<String, Object> importExcel(MultipartFile file, boolean generateStatic);
}
