package net.mingsoft.cms.biz.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import net.mingsoft.base.biz.impl.BaseBizImpl;
import net.mingsoft.base.dao.IBaseDao;
import net.mingsoft.base.exception.BusinessException;
import net.mingsoft.basic.util.BasicUtil;
import net.mingsoft.cms.biz.IManualTemplateBiz;
import net.mingsoft.cms.dao.IManualTemplateDao;
import net.mingsoft.cms.entity.ManualTemplateEntity;
import net.mingsoft.cms.util.ManualRenderUtil;
import net.mingsoft.mdiy.biz.IModelBiz;
import net.mingsoft.mdiy.entity.ModelEntity;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 说明书模板业务实现
 * @version
 * 版本号：1.0.0<br/>
 * 创建日期：2026-08-30<br/>
 */
@Service("manualTemplateBizImpl")
@Transactional
public class ManualTemplateBizImpl extends BaseBizImpl<IManualTemplateDao, ManualTemplateEntity> implements IManualTemplateBiz {

	/**
	 * 产品规格模型表名（说明书占位符数据源，加字段后占位符清单自动扩展）
	 */
	public static final String SPEC_TABLE = "MDIY_MODEL_CPSPECPROD";

	/**
	 * 模板文件存放目录（upload/{appId}/manual/）
	 */
	private static final String MANUAL_DIR = "manual";

	@Autowired
	private IModelBiz modelBiz;

	@Autowired
	private IManualTemplateDao manualTemplateDao;

	@Autowired
	private net.mingsoft.cms.dao.IContentDao contentDao;

	/**
	 * 静态页面输出目录（与静态化模块一致）
	 */
	@org.springframework.beans.factory.annotation.Value("${ms.diy.html-dir:html}")
	private String htmlDir;

	@Override
	protected IBaseDao getDao() {
		return manualTemplateDao;
	}

	// ==================== 模板管理 ====================

	@Override
	public ManualTemplateEntity saveTemplate(MultipartFile file, String templateName, String templateLang, String remark) {
		if (StringUtils.isBlank(templateName)) {
			throw new BusinessException("模板名称不能为空");
		}
		String html = readAndValidate(file);

		ManualTemplateEntity entity = new ManualTemplateEntity();
		entity.setTemplateName(templateName);
		entity.setTemplateLang("en".equalsIgnoreCase(templateLang) ? "en" : "cn");
		entity.setTemplateUrl(saveTemplateFile(file, html));
		entity.setTemplateSize(html.getBytes(StandardCharsets.UTF_8).length);
		entity.setPlaceholders(extractPlaceholders(html));
		entity.setStatus("1");
		entity.setRemark(remark);
		entity.setCreateDate(new Date());
		if (BasicUtil.getManager() != null) {
			entity.setCreateBy(String.valueOf(BasicUtil.getManager().getId()));
		}
		this.save(entity);
		return entity;
	}

	@Override
	public ManualTemplateEntity replaceTemplateFile(String id, MultipartFile file) {
		ManualTemplateEntity entity = getTemplate(id);
		String html = readAndValidate(file);
		String oldUrl = entity.getTemplateUrl();
		entity.setTemplateUrl(saveTemplateFile(file, html));
		entity.setTemplateSize(html.getBytes(StandardCharsets.UTF_8).length);
		entity.setPlaceholders(extractPlaceholders(html));
		entity.setUpdateDate(new Date());
		if (BasicUtil.getManager() != null) {
			entity.setUpdateBy(String.valueOf(BasicUtil.getManager().getId()));
		}
		this.updateById(entity);
		// 新文件写入成功后删除旧文件（不留历史，绑定产品下次下载即用新模板）
		deleteFileByUrl(oldUrl);
		return entity;
	}

	@Override
	public void deleteTemplate(String id) {
		ManualTemplateEntity entity = getTemplate(id);
		int bindCount = getBindCount(id);
		if (bindCount > 0) {
			throw new BusinessException("该模板已绑定" + bindCount + "个产品，请先解绑后再删除");
		}
		this.removeById(id);
		deleteFileByUrl(entity.getTemplateUrl());
	}

	@Override
	public List<Map<String, Object>> listWithCount() {
		List<ManualTemplateEntity> list = this.lambdaQuery().orderByDesc(ManualTemplateEntity::getUpdateDate)
				.orderByDesc(ManualTemplateEntity::getCreateDate).list();
		// 绑定数统计（一次GROUP BY）
		Map<String, Object> bindCount = new HashMap<>();
		for (Map<String, Object> row : queryForList("SELECT TEMPLATE_ID AS tid, COUNT(*) AS cnt FROM "
				+ SPEC_TABLE + " WHERE TEMPLATE_ID IS NOT NULL GROUP BY TEMPLATE_ID")) {
			bindCount.put(String.valueOf(row.get("tid")), row.get("cnt"));
		}
		List<Map<String, Object>> result = new ArrayList<>();
		for (ManualTemplateEntity entity : list) {
			Map<String, Object> item = new HashMap<>();
			item.put("id", entity.getId());
			item.put("templateName", entity.getTemplateName());
			item.put("templateLang", entity.getTemplateLang());
			item.put("templateUrl", entity.getTemplateUrl());
			item.put("templateSize", entity.getTemplateSize());
			item.put("placeholders", entity.getPlaceholders());
			item.put("status", entity.getStatus());
			item.put("remark", entity.getRemark());
			item.put("updateDate", entity.getUpdateDate() != null ? entity.getUpdateDate() : entity.getCreateDate());
			item.put("bindCount", bindCount.getOrDefault(entity.getId(), 0));
			result.add(item);
		}
		return result;
	}

	// ==================== 占位符字段 ====================

	@Override
	public List<Map<String, Object>> getFieldList() {
		ModelEntity model = getSpecModel();
		if (model == null) {
			throw new BusinessException("未找到产品规格模型:" + SPEC_TABLE);
		}
		List<Map> fields = JSONUtil.toList(model.getModelField(), Map.class);
		List<Map<String, Object>> result = new ArrayList<>();
		if (fields != null) {
			for (Map field : fields) {
				Map<String, Object> item = new HashMap<>();
				item.put("key", field.get("key"));
				item.put("name", field.get("name"));
				result.add(item);
			}
		}
		return result;
	}

	// ==================== 绑定管理 ====================

	@Override
	public Map<String, Object> queryProducts(String categoryId, String search, String bindFilter, String currentTemplateId, int page, int size) {
		StringBuilder where = new StringBuilder(" WHERE 1=1 ");
		List<Object> params = new ArrayList<>();
		if (StringUtils.isNotBlank(categoryId) && !"0".equals(categoryId)) {
			// 栏目筛选含全部子孙栏目（选中父栏目如"产品中心"可查出子栏目下的产品）
			where.append(" AND c.CATEGORY_ID IN (SELECT c2.id FROM cms_category c2 ")
					.append("WHERE c2.del = 0 AND (c2.id = ? OR FIND_IN_SET(?, c2.category_parent_ids))) ");
			params.add(categoryId);
			params.add(categoryId);
		}
		if (StringUtils.isNotBlank(search)) {
			// 搜索支持货号/产品标题/栏目标题（栏目匹配时含其全部子孙栏目，如"产品中心"可搜出子栏目产品）
			where.append(" AND (s.CATALOG_NO LIKE ? OR c.CONTENT_TITLE LIKE ? OR c.CATEGORY_ID IN (")
					.append("SELECT c2.id FROM cms_category c2 WHERE c2.del = 0 AND EXISTS (")
					.append("SELECT 1 FROM cms_category c3 WHERE c3.del = 0 AND c3.category_title LIKE ? ")
					.append("AND (c2.id = c3.id OR FIND_IN_SET(c3.id, c2.category_parent_ids))))) ");
			String like = "%" + search.trim() + "%";
			params.add(like);
			params.add(like);
			params.add(like);
		}
		if ("bind".equals(bindFilter)) {
			where.append(" AND s.TEMPLATE_ID IS NOT NULL ");
		} else if ("unbind".equals(bindFilter)) {
			where.append(" AND s.TEMPLATE_ID IS NULL ");
		} else if ("other".equals(bindFilter) && StringUtils.isNotBlank(currentTemplateId)) {
			where.append(" AND s.TEMPLATE_ID IS NOT NULL AND s.TEMPLATE_ID <> ? ");
			params.add(currentTemplateId);
		}

		String countSql = "SELECT COUNT(*) FROM " + SPEC_TABLE + " s INNER JOIN cms_content c ON c.ID = s.LINK_ID "
				+ "LEFT JOIN cms_category ca ON ca.ID = c.CATEGORY_ID" + where;
		int total = ((Number) queryForList(countSql, params.toArray()).get(0).values().iterator().next()).intValue();

		String dataSql = "SELECT c.ID AS id, c.CONTENT_TITLE AS title, s.CATALOG_NO AS catalogNo, s.TEMPLATE_ID AS templateId, "
				+ "c.CATEGORY_ID AS categoryId, ca.CATEGORY_TITLE AS categoryTitle FROM " + SPEC_TABLE + " s "
				+ "INNER JOIN cms_content c ON c.ID = s.LINK_ID LEFT JOIN cms_category ca ON ca.ID = c.CATEGORY_ID" + where
				+ " ORDER BY c.ID DESC LIMIT ? OFFSET ? ";
		List<Object> dataParams = new ArrayList<>(params);
		dataParams.add(size);
		dataParams.add((Math.max(page, 1) - 1) * size);

		// 模板名称对照
		Map<String, String> nameMap = new HashMap<>();
		for (ManualTemplateEntity t : this.list()) {
			nameMap.put(t.getId(), t.getTemplateName());
		}

		List<Map<String, Object>> rows = new ArrayList<>();
		for (Map<String, Object> row : queryForList(dataSql, dataParams.toArray())) {
			row.put("templateName", nameMap.get(String.valueOf(row.get("templateId"))));
			rows.add(row);
		}
		Map<String, Object> result = new HashMap<>();
		result.put("rows", rows);
		result.put("total", total);
		return result;
	}

	@Override
	public int saveBind(String templateId, List<String> productIds) {
		if (productIds == null || productIds.isEmpty()) {
			throw new BusinessException("请先勾选产品");
		}
		if (productIds.size() > 2000) {
			throw new BusinessException("单次最多绑定2000个产品");
		}
		if (StringUtils.isNotBlank(templateId)) {
			// 绑定的模板必须存在且启用
			getTemplate(templateId);
		}
		// 一个产品只能绑一个模板：直接覆盖更新（后保存覆盖先保存）；解绑时同步清空MANUAL附件（前台下载按钮随之消失）
		String placeholders = productIds.stream().map(p -> "?").collect(Collectors.joining(","));
		int rows;
		if (StringUtils.isBlank(templateId)) {
			rows = update("UPDATE " + SPEC_TABLE + " SET TEMPLATE_ID = NULL, MANUAL = NULL, UPDATE_DATE = NOW() WHERE LINK_ID IN (" + placeholders + ")",
					productIds.toArray());
			// 解绑后MANUAL字段已清空，对应PDF成为孤儿文件，立即删除避免残留（文件名=文章id，与模板无关）
			deletePdfFiles(productIds);
		} else {
			rows = update("UPDATE " + SPEC_TABLE + " SET TEMPLATE_ID = ?, UPDATE_DATE = NOW() WHERE LINK_ID IN (" + placeholders + ")",
					buildBindParams(templateId, productIds));
		}
		// 刷新文章更新时间：首次绑定/解绑影响静态页下载按钮显示，可通过"生成文章"按时间重新生成
		update("UPDATE cms_content SET UPDATE_DATE = NOW() WHERE ID IN (" + placeholders + ")", productIds.toArray());
		return rows;
	}

	// ==================== 说明书渲染 ====================

	@Override
	public Map<String, Object> renderManual(String linkId, String templateId) {
		if (StringUtils.isBlank(linkId)) {
			throw new BusinessException("产品编号不能为空");
		}
		List<Map<String, Object>> specRows = queryForList("SELECT * FROM " + SPEC_TABLE + " WHERE LINK_ID = ?", linkId);
		if (specRows.isEmpty()) {
			throw new BusinessException("该产品无规格数据");
		}
		Map<String, Object> specData = specRows.get(0);
		String tid = StringUtils.isNotBlank(templateId) ? templateId : String.valueOf(specData.get("TEMPLATE_ID"));
		if (StringUtils.isBlank(tid) || "null".equals(tid)) {
			throw new BusinessException("该产品未绑定说明书模板");
		}
		ManualTemplateEntity entity = getTemplate(tid);
		if (!"1".equals(entity.getStatus())) {
			throw new BusinessException("说明书模板已停用");
		}

		String html = readTemplateContent(entity.getTemplateUrl());
		// 字段默认值：规格未填时默认96T（可按产品数据覆盖）
		Map<String, String> defaults = new HashMap<>();
		defaults.put("SPEC", "96T");
		String substituted = ManualRenderUtil.substitute(html, specData, defaults);
		// baseUri=模板文件所在目录，模板里的相对路径图片（如原理图/标曲图）按此解析
		File templateFile = resolveUrl(entity.getTemplateUrl());
		String baseUri = templateFile.getParentFile().toURI().toString();
		byte[] pdf = ManualRenderUtil.htmlToPdf(substituted, baseUri);

		String catalogNo = String.valueOf(specData.get("CATALOG_NO"));
		if (StringUtils.isBlank(catalogNo) || "null".equals(catalogNo)) {
			catalogNo = linkId;
		}
		Map<String, Object> result = new HashMap<>();
		result.put("pdf", pdf);
		result.put("fileName", catalogNo + ("en".equalsIgnoreCase(entity.getTemplateLang()) ? "-Manual.pdf" : "-说明书.pdf"));
		result.put("catalogNo", catalogNo);
		return result;
	}

	@Override
	public Map<String, Object> previewTemplate(String templateId) {
		ManualTemplateEntity entity = getTemplate(templateId);
		// 原始模板直接转PDF，占位符{{X}}原样显示（不填值）
		String html = readTemplateContent(entity.getTemplateUrl());
		File templateFile = resolveUrl(entity.getTemplateUrl());
		String baseUri = templateFile.getParentFile().toURI().toString();
		byte[] pdf = ManualRenderUtil.htmlToPdf(html, baseUri);
		Map<String, Object> result = new HashMap<>();
		result.put("pdf", pdf);
		result.put("fileName", entity.getTemplateName() + "-模板预览.pdf");
		return result;
	}

	@Override
	public String getTemplateContent(String templateId) {
		ManualTemplateEntity entity = getTemplate(templateId);
		return readTemplateContent(entity.getTemplateUrl());
	}

	@Override
	public Map<String, Object> downloadTemplate(String templateId) {
		ManualTemplateEntity entity = getTemplate(templateId);
		String html = readTemplateContent(entity.getTemplateUrl());
		Map<String, Object> result = new HashMap<>();
		result.put("bytes", html.getBytes(StandardCharsets.UTF_8));
		// 文件名取模板名（去空格）+.html
		String safeName = entity.getTemplateName().trim().replaceAll("[\\\\/:*?\"<>|]", "_");
		result.put("fileName", safeName + ".html");
		return result;
	}

	@Override
	public void updateTemplateContent(String templateId, String html) {
		if (StringUtils.isBlank(html)) {
			throw new BusinessException("模板内容不能为空");
		}
		ManualTemplateEntity entity = getTemplate(templateId);
		// 保存前干跑一次HTML转PDF：语法错误直接抛出，避免写坏模板导致前台下载500
		File templateFile = resolveUrl(entity.getTemplateUrl());
		String baseUri = templateFile.getParentFile().toURI().toString();
		ManualRenderUtil.htmlToPdf(html, baseUri);
		try {
			FileUtils.writeStringToFile(templateFile, html, StandardCharsets.UTF_8);
		} catch (Exception e) {
			throw new BusinessException("模板文件写入失败:" + e.getMessage());
		}
		entity.setTemplateSize(html.getBytes(StandardCharsets.UTF_8).length);
		entity.setPlaceholders(extractPlaceholders(html));
		entity.setUpdateDate(new Date());
		if (BasicUtil.getManager() != null) {
			entity.setUpdateBy(String.valueOf(BasicUtil.getManager().getId()));
		}
		this.updateById(entity);
	}

	// ==================== 一键生成附件（静态化说明书） ====================

	@Override
	public Map<String, Object> generateAttachments(String templateId) {
		ManualTemplateEntity entity = getTemplate(templateId);
		if (!"1".equals(entity.getStatus())) {
			throw new BusinessException("说明书模板已停用");
		}
		// 绑定的产品（仅已发布、未删除且栏目启用的才生成）
		List<Map<String, Object>> products = queryForList(
				"SELECT s.LINK_ID AS linkId, s.CATALOG_NO AS catalogNo FROM " + SPEC_TABLE + " s "
						+ "INNER JOIN cms_content c ON c.ID = s.LINK_ID "
						+ "LEFT JOIN cms_category ca ON ca.ID = c.CATEGORY_ID "
						+ "WHERE s.TEMPLATE_ID = ? AND c.del = 0 AND c.content_display = '0' AND ca.category_display = 'enable'",
				templateId);
		Map<String, Object> result = new HashMap<>();
		result.put("total", products.size());
		if (products.isEmpty()) {
			result.put("success", 0);
			result.put("pages", 0);
			result.put("errors", new ArrayList<>());
			return result;
		}
		File dir = manualDir();
		List<String> okIds = new ArrayList<>();
		List<String> errors = new ArrayList<>();
		for (Map<String, Object> p : products) {
			String linkId = String.valueOf(p.get("linkId"));
			String catalogNo = String.valueOf(p.get("catalogNo"));
			try {
				// 1. 渲染PDF并落盘（文件名用文章id，重复生成直接覆盖不产生垃圾文件）
				Map<String, Object> rendered = renderManual(linkId, templateId);
				File pdfFile = new File(dir, linkId + ".pdf");
				FileUtils.writeByteArrayToFile(pdfFile, (byte[]) rendered.get("pdf"));
				// 2. 回填MANUAL附件字段（前台详情页"资料下载"读取此字段）
				String displayName = String.valueOf(rendered.get("fileName"));
				String url = toUrl(pdfFile);
				String json = "[{\"url\":\"" + url + "\",\"name\":\"" + displayName.replace("\\", "\\\\").replace("\"", "\\\"") + "\"}]";
				update("UPDATE " + SPEC_TABLE + " SET MANUAL = ? WHERE LINK_ID = ?", json, linkId);
				okIds.add(linkId);
			} catch (Exception e) {
				errors.add(catalogNo + "/" + linkId + ": " + e.getMessage());
				LOG.error("说明书附件生成失败 linkId={}", linkId, e);
			}
		}
		// 3. 对生成成功的产品定向静态化（复用栏目静态化管线，仅生成选中的文章）
		int pages = 0;
		if (!okIds.isEmpty()) {
			List<net.mingsoft.cms.bean.CategoryBean> beans = contentDao.queryBeansByArticleIds(okIds);
			net.mingsoft.cms.util.CmsParserUtil.generateBasic(beans, htmlDir, null);
			pages = beans.size();
		}
		result.put("success", okIds.size());
		result.put("pages", pages);
		result.put("errors", errors);
		return result;
	}

	// ==================== 磁盘治理 ====================

	@Override
	public Map<String, Object> diskScan() {
		File dir = manualDir();
		List<Map<String, Object>> orphans = new ArrayList<>();
		long templateSize = 0, orphanSize = 0;
		int templates = 0;
		// 数据库中模板文件URL集合：HTML按此判定是否孤儿
		Set<String> dbUrls = this.list().stream()
				.map(ManualTemplateEntity::getTemplateUrl).collect(Collectors.toSet());
		// 数据库中仍被MANUAL字段引用的PDF文件名集合（文件名=文章id.pdf）
		Set<String> referencedPdfs = queryForList("SELECT DISTINCT MANUAL FROM " + SPEC_TABLE + " WHERE MANUAL IS NOT NULL")
				.stream()
				.map(r -> String.valueOf(r.get("MANUAL")))
				.filter(m -> StringUtils.isNotBlank(m) && !"null".equals(m))
				.flatMap(m -> {
					// MANUAL为JSON数组[{"url":"...","name":"..."}]，提取url的文件名部分
					try {
						return cn.hutool.json.JSONUtil.toList(m, Map.class).stream()
								.map(x -> String.valueOf(x.get("url")))
								.filter(u -> u.toLowerCase().endsWith(".pdf"))
								.map(u -> u.substring(u.lastIndexOf('/') + 1));
					} catch (Exception e) {
						return java.util.stream.Stream.empty();
					}
				})
				.collect(Collectors.toSet());
		File[] files = dir.listFiles();
		if (files != null) {
			for (File f : files) {
				String lower = f.getName().toLowerCase();
				if (!f.isFile()) {
					continue;
				}
				// 说明书PDF附件：文件名=文章id.pdf，DB无MANUAL引用即为孤儿（解绑/换绑残留），同样防误杀刚生成的文件
				if (lower.endsWith(".pdf")) {
					if (referencedPdfs.contains(f.getName())) {
						templateSize += f.length();
					} else if (System.currentTimeMillis() - f.lastModified() > 24L * 3600 * 1000) {
						Map<String, Object> o = new HashMap<>();
						o.put("name", f.getName());
						o.put("size", f.length());
						o.put("lastModified", new Date(f.lastModified()));
						orphans.add(o);
						orphanSize += f.length();
					}
					continue;
				}
				// 模板HTML文件；图片等资源文件（模板引用）跳过
				if (!lower.endsWith(".html")) {
					continue;
				}
				String url = toUrl(f);
				if (dbUrls.contains(url)) {
					templates++;
					templateSize += f.length();
				} else if (System.currentTimeMillis() - f.lastModified() > 24L * 3600 * 1000) {
					// 无DB记录且超过1天（防误杀正在上传的文件）
					Map<String, Object> o = new HashMap<>();
					o.put("name", f.getName());
					o.put("size", f.length());
					o.put("lastModified", new Date(f.lastModified()));
					orphans.add(o);
					orphanSize += f.length();
				}
			}
		}
		Map<String, Object> result = new HashMap<>();
		result.put("templates", templates);
		result.put("templateSize", templateSize);
		result.put("orphans", orphans);
		result.put("orphanSize", orphanSize);
		return result;
	}

	@Override
	public int diskClean() {
		Map<String, Object> scan = diskScan();
		List<Map<String, Object>> orphans = (List<Map<String, Object>>) scan.get("orphans");
		int cleaned = 0;
		for (Map<String, Object> o : orphans) {
			File f = new File(manualDir(), String.valueOf(o.get("name")));
			if (f.delete()) {
				cleaned++;
			}
		}
		return cleaned;
	}

	// ==================== 私有方法 ====================

	/**
	 * 获取模板实体（含存在性校验）
	 */
	private ManualTemplateEntity getTemplate(String id) {
		if (StringUtils.isBlank(id)) {
			throw new BusinessException("模板编号不能为空");
		}
		ManualTemplateEntity entity = this.getById(id);
		if (entity == null) {
			throw new BusinessException("说明书模板不存在");
		}
		return entity;
	}

	/**
	 * 模板绑定产品数
	 */
	private int getBindCount(String templateId) {
		List<Map<String, Object>> rows = queryForList(
				"SELECT COUNT(*) AS cnt FROM " + SPEC_TABLE + " WHERE TEMPLATE_ID = ?", templateId);
		return ((Number) rows.get(0).get("cnt")).intValue();
	}

	/**
	 * 规格模型实体（按表名查）
	 */
	private ModelEntity getSpecModel() {
		List<Map<String, Object>> rows = queryForList(
				"SELECT ID FROM mdiy_model WHERE MODEL_TABLE_NAME = ?", SPEC_TABLE);
		if (rows.isEmpty()) {
			return null;
		}
		return modelBiz.getEntityById(String.valueOf(rows.get(0).values().iterator().next()));
	}

	/**
	 * 校验上传的模板文件：后缀html/htm、非空、UTF-8可读
	 */
	private String readAndValidate(MultipartFile file) {
		if (file == null || file.isEmpty()) {
			throw new BusinessException("请选择模板HTML文件");
		}
		String name = file.getOriginalFilename();
		String suffix = name != null && name.lastIndexOf('.') >= 0 ? name.substring(name.lastIndexOf('.') + 1).toLowerCase() : "";
		if (!"html".equals(suffix) && !"htm".equals(suffix)) {
			throw new BusinessException("模板文件仅支持.html/.htm格式");
		}
		try {
			String html = new String(file.getBytes(), StandardCharsets.UTF_8);
			if (StringUtils.isBlank(html)) {
				throw new BusinessException("模板文件内容为空");
			}
			return html;
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new BusinessException("模板文件读取失败:" + e.getMessage());
		}
	}

	/**
	 * 模板文件落盘：upload/{appId}/manual/{雪花id}.html，返回访问URL
	 */
	private String saveTemplateFile(MultipartFile file, String html) {
		try {
			File dir = manualDir();
			FileUtils.forceMkdir(dir);
			String fileName = IdUtil.getSnowflake().nextId() + ".html";
			File target = new File(dir, fileName);
			FileUtils.writeStringToFile(target, html, StandardCharsets.UTF_8);
			return toUrl(target);
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new BusinessException("模板文件保存失败:" + e.getMessage());
		}
	}

	/**
	 * 读取模板文件内容
	 */
	private String readTemplateContent(String url) {
		File f = resolveUrl(url);
		if (!f.exists()) {
			throw new BusinessException("模板文件不存在:" + url);
		}
		try {
			return FileUtils.readFileToString(f, StandardCharsets.UTF_8);
		} catch (Exception e) {
			throw new BusinessException("模板文件读取失败:" + e.getMessage());
		}
	}

	/**
	 * 删除模板文件
	 */
	private void deleteFileByUrl(String url) {
		if (StringUtils.isBlank(url)) {
			return;
		}
		File f = resolveUrl(url);
		if (f.exists() && !f.delete()) {
			LOG.warn("模板文件删除失败:{}", url);
		}
	}

	/**
	 * 删除产品说明书PDF附件（解绑时调用：MANUAL字段已清空，PDF成为孤儿文件）
	 * 文件名=文章id.pdf，与模板无关；不存在则跳过（从未生成过附件的产品）
	 */
	private void deletePdfFiles(List<String> linkIds) {
		File dir = manualDir();
		for (String linkId : linkIds) {
			File pdf = new File(dir, linkId + ".pdf");
			if (pdf.exists() && !pdf.delete()) {
				LOG.warn("说明书PDF删除失败:{}", pdf.getAbsolutePath());
			}
		}
	}

	/**
	 * 模板文件目录：{webapp根}/upload/{appId}/manual
	 */
	private File manualDir() {
		String uploadRoot = BasicUtil.getRealPath("upload");
		File dir = new File(uploadRoot, BasicUtil.getApp().getAppId() + File.separator + MANUAL_DIR);
		if (!dir.exists()) {
			dir.mkdirs();
		}
		return dir;
	}

	/**
	 * 磁盘文件 -> 访问URL（/upload/{appId}/manual/{name}）
	 */
	private String toUrl(File f) {
		String uploadRoot = new File(BasicUtil.getRealPath("upload")).getAbsolutePath();
		String path = f.getAbsolutePath().replace(uploadRoot, "").replace("\\", "/");
		return ("/upload" + path).replace("//", "/");
	}

	/**
	 * 访问URL -> 磁盘文件
	 */
	private File resolveUrl(String url) {
		String relative = url.replaceFirst("^/upload", "");
		return new File(BasicUtil.getRealPath("upload"), relative);
	}

	/**
	 * 提取模板中的占位符（{{X}} -> X，逗号分隔）
	 */
	private String extractPlaceholders(String html) {
		Set<String> set = new LinkedHashSet<>();
		java.util.regex.Matcher m = java.util.regex.Pattern.compile("\\{\\{([A-Za-z0-9_]+)\\}\\}").matcher(html);
		while (m.find()) {
			set.add(m.group(1));
		}
		return String.join(",", set);
	}

	/**
	 * 绑定SQL参数（解绑时TEMPLATE_ID为null）
	 */
	private Object[] buildBindParams(String templateId, List<String> productIds) {
		Object[] params = new Object[productIds.size() + 1];
		params[0] = StringUtils.isBlank(templateId) ? null : templateId;
		for (int i = 0; i < productIds.size(); i++) {
			params[i + 1] = productIds.get(i);
		}
		return params;
	}
}
