package net.mingsoft.cms.biz;

import net.mingsoft.base.biz.IBaseBiz;
import net.mingsoft.cms.entity.ManualTemplateEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 说明书模板业务接口
 * @version
 * 版本号：1.0.0<br/>
 * 创建日期：2026-08-30<br/>
 */
public interface IManualTemplateBiz extends IBaseBiz<ManualTemplateEntity> {

	/**
	 * 新建模板（上传HTML文件+校验占位符）
	 * @param file 模板HTML文件
	 * @param templateName 模板名称
	 * @param templateLang 语言 cn/en
	 * @param remark 备注
	 * @return 保存后的实体（含占位符校验结果）
	 */
	ManualTemplateEntity saveTemplate(MultipartFile file, String templateName, String templateLang, String remark);

	/**
	 * 替换模板文件（删旧文件不留历史，绑定产品下次下载即用新模板）
	 * @param id 模板id
	 * @param file 新模板HTML文件
	 * @return 更新后的实体
	 */
	ManualTemplateEntity replaceTemplateFile(String id, MultipartFile file);

	/**
	 * 删除模板（仅允许删除绑定数为0的模板）
	 * @param id 模板id
	 */
	void deleteTemplate(String id);

	/**
	 * 模板列表（含绑定产品数统计）
	 * @return 每项含实体字段 + bindCount
	 */
	List<Map<String, Object>> listWithCount();

	/**
	 * 规格表可用占位符字段清单（动态读取模型字段定义，规格表加字段后自动出现）
	 * @return [{key:列名, name:显示名}]
	 */
	List<Map<String, Object>> getFieldList();

	/**
	 * 绑定页产品列表（栏目/货号过滤，带当前绑定模板信息）
	 * @param categoryId 栏目id（空=全部）
	 * @param search 货号/标题模糊搜索
	 * @param bindFilter 绑定状态过滤：空=全部，bind=已绑模板，unbind=未绑定，other=已绑其他模板
	 * @param currentTemplateId 当前模板id（用于other判断）
	 * @param page 页码
	 * @param size 每页条数
	 * @return {rows:[{id,title,catalogNo,templateId,templateName,categoryId,categoryTitle}], total}
	 */
	Map<String, Object> queryProducts(String categoryId, String search, String bindFilter, String currentTemplateId, int page, int size);

	/**
	 * 批量保存绑定（一个产品只能绑一个模板，后保存覆盖先保存）
	 * 同时刷新文章更新时间，供"生成文章"按时间重新静态化（首次绑定/解绑影响前台按钮显示）
	 * @param templateId 模板id（空=解绑）
	 * @param productIds 文章id列表
	 * @return 受影响行数
	 */
	int saveBind(String templateId, List<String> productIds);

	/**
	 * 渲染某产品的说明书PDF（模板+规格数据实时生成，不落盘）
	 * @param linkId 文章id
	 * @param templateId 模板id（空则取产品当前绑定）
	 * @return {pdf:PDF字节, fileName:下载文件名, catalogNo:货号}
	 */
	Map<String, Object> renderManual(String linkId, String templateId);

	/**
	 * 预览模板本身：占位符不替换（{{X}}原样显示），用于查看模板原始效果，无需绑定产品
	 * @param templateId 模板id
	 * @return {pdf:PDF字节, fileName:预览文件名}
	 */
	Map<String, Object> previewTemplate(String templateId);

	/**
	 * 读取模板HTML内容（在线编辑用）
	 * @param templateId 模板id
	 * @return 模板HTML全文
	 */
	String getTemplateContent(String templateId);

	/**
	 * 保存在线编辑的模板内容（覆写模板文件并刷新占位符/大小/更新时间，绑定产品下次下载即生效）
	 * @param templateId 模板id
	 * @param html 编辑后的模板HTML全文
	 */
	void updateTemplateContent(String templateId, String html);

	/**
	 * 下载模板HTML源文件
	 * @param templateId 模板id
	 * @return {bytes:文件字节, fileName:下载文件名}
	 */
	Map<String, Object> downloadTemplate(String templateId);

	/**
	 * 一键生成附件：对该模板已绑定的全部产品批量生成说明书PDF（写入upload目录），
	 * 回填产品规格模型的MANUAL附件字段，并对这些产品定向静态化（页面立即生效）
	 * @param templateId 模板id
	 * @return {total:绑定产品数, success:成功数, pages:静态化页面数, errors:失败明细}
	 */
	Map<String, Object> generateAttachments(String templateId);

	/**
	 * 读取产品已生成的说明书静态附件（前台下载接口用，替代实时渲染）
	 * @param linkId 文章id
	 * @return {bytes:PDF字节, fileName:下载文件名}
	 */
	Map<String, Object> getManualAttachment(String linkId);

	/**
	 * 说明书目录扫描（孤儿文件检测）
	 * @return {templates:模板文件数, templateSize:字节数, orphans:[{name,size,lastModified}], orphanSize}
	 */
	Map<String, Object> diskScan();

	/**
	 * 清理孤儿文件（DB无记录且修改时间超过1天）
	 * @return 清理数量
	 */
	int diskClean();
}
