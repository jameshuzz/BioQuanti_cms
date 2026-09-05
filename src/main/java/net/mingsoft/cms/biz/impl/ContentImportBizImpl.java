package net.mingsoft.cms.biz.impl;

import cn.hutool.core.map.CaseInsensitiveMap;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import net.mingsoft.basic.util.BasicUtil;
import net.mingsoft.cms.bean.CategoryBean;
import net.mingsoft.cms.bean.ContentBean;
import net.mingsoft.cms.biz.ICategoryBiz;
import net.mingsoft.cms.constant.e.CategoryTypeEnum;
import net.mingsoft.cms.biz.IContentBiz;
import net.mingsoft.cms.biz.IContentImportBiz;
import net.mingsoft.cms.entity.CategoryEntity;
import net.mingsoft.cms.entity.ContentEntity;
import net.mingsoft.cms.util.CmsParserUtil;
import net.mingsoft.mdiy.biz.IModelBiz;
import net.mingsoft.mdiy.biz.IModelDataBiz;
import net.mingsoft.mdiy.entity.ModelEntity;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * 文章Excel导入业务实现
 * Excel列 = 文章字段（标题/栏目/缩略图/内容HTML）+ 产品规格字段（货号/名称/表格数据等）
 * 货号已存在则更新对应文章和规格数据，否则新增
 * @version
 * 版本号：1.0.0<br/>
 * 创建日期：2026-09-02<br/>
 */
@Service("contentImportBizImpl")
public class ContentImportBizImpl implements IContentImportBiz {

	private static final Logger LOG = LoggerFactory.getLogger(ContentImportBizImpl.class);

	/**
	 * 产品规格模型表名（与ManualTemplateBizImpl一致）
	 */
	private static final String SPEC_TABLE = "mdiy_model_cpspecprod";

	/**
	 * 文章标签字典类型（与文章编辑页标签选择器同源：中文/zh、英文/en）
	 */
	private static final String TAG_DICT_TYPE = "文章标签";

	/**
	 * Excel表头 -> 数据键 映射（文章字段 + 规格模型字段model名）
	 */
	private static final LinkedHashMap<String, String> HEADER_MAP = new LinkedHashMap<>();

	static {
		HEADER_MAP.put("标题", "contentTitle");
		HEADER_MAP.put("栏目", "categoryTitle");
		HEADER_MAP.put("标签", "contentTags");
		HEADER_MAP.put("缩略图URL", "contentImg");
		HEADER_MAP.put("文章内容HTML", "contentDetails");
		HEADER_MAP.put("货号", "catalogNo");
		HEADER_MAP.put("产品名称(中文)", "productCn");
		HEADER_MAP.put("产品名称(英文)", "productEn");
		HEADER_MAP.put("检测物名称", "sampleName");
		HEADER_MAP.put("规格", "spec");
		HEADER_MAP.put("灵敏度", "sensitivity");
		HEADER_MAP.put("检测范围", "detectRange");
		HEADER_MAP.put("标准品最高浓度", "stdTop");
		HEADER_MAP.put("梯度稀释浓度串", "stdDilutionSeries");
		HEADER_MAP.put("交叉反应浓度", "crossReactConc");
		HEADER_MAP.put("背景信息", "background");
		HEADER_MAP.put("标准曲线数据", "calTable");
		HEADER_MAP.put("参考样本数据", "sampleTable");
		HEADER_MAP.put("精密度数据", "precisionTable");
		HEADER_MAP.put("回收率数据", "recoveryTable");
		HEADER_MAP.put("线性数据", "linearityTable");
		HEADER_MAP.put("参考文献", "literature");
	}

	@Autowired
	private IContentBiz contentBiz;

	@Autowired
	private ICategoryBiz categoryBiz;

	@Autowired
	private IModelBiz modelBiz;

	@Autowired
	private IModelDataBiz modelDataBiz;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	/**
	 * 静态页面输出目录（与静态化模块一致）
	 */
	@Value("${ms.diy.html-dir:html}")
	private String htmlDir;

	@Override
	public void downloadTemplate(HttpServletResponse response) {
		try (XSSFWorkbook wb = new XSSFWorkbook()) {
			XSSFSheet sheet = wb.createSheet("文章导入");
			// 表头样式
			XSSFCellStyle headStyle = wb.createCellStyle();
			XSSFFont font = wb.createFont();
			font.setBold(true);
			headStyle.setFont(font);

			// 表头行
			XSSFRow head = sheet.createRow(0);
			int col = 0;
			for (String title : HEADER_MAP.keySet()) {
				XSSFCell c = head.createCell(col++);
				c.setCellValue(title);
				c.setCellStyle(headStyle);
			}

			// 示例行
			XSSFRow sample = sheet.createRow(1);
			col = 0;
			sample.createCell(col++).setCellValue("人C反应蛋白(CRP) ELISA试剂盒（示例，导入前请删除本行）");
			sample.createCell(col++).setCellValue("");
			sample.createCell(col++).setCellValue("中文");
			sample.createCell(col++).setCellValue("");
			sample.createCell(col++).setCellValue("<h3>产品介绍</h3><p>此处填写产品介绍，支持HTML。</p>");
			sample.createCell(col++).setCellValue("BQE-00000");
			sample.createCell(col++).setCellValue("人C反应蛋白(CRP) ELISA试剂盒");
			sample.createCell(col++).setCellValue("Human CRP ELISA Kit");
			sample.createCell(col++).setCellValue("人CRP");
			sample.createCell(col++).setCellValue("96T");
			sample.createCell(col++).setCellValue("0.938ug/mL");
			sample.createCell(col++).setCellValue("1.563-100ng/mL");
			sample.createCell(col++).setCellValue("1000");
			sample.createCell(col++).setCellValue("500pg/mL，250pg/mL，125pg/mL，62.5pg/mL，31.25pg/mL，15.63pg/mL，0pg/mL");
			sample.createCell(col++).setCellValue("50ng/mL");
			sample.createCell(col++).setCellValue("检测原理段落文本");
			sample.createCell(col++).setCellValue("STD.(pg/mL)\tOD-1\tOD-2\tAverage\tCorrected\n1000\t2.1\t2.0\t2.05\t1.95");
			sample.createCell(col++).setCellValue("样本类型\t推荐稀释比例\t参考含量\nSerum or Plasma\t1/2000-1/20,000 dilution\t1.1-5.9 ug/ml");
			sample.createCell(col++).setCellValue("均值(pg/mL)\t217.7\t988.0\t1464.4\t213.4\t1008.6\t1506.6\n标准差\t8.82\t64.02\t83.03\t12.10\t40.85\t97.6\n变异系数(%)\t4.1\t6.5\t5.7\t5.7\t4.1\t6.5");
			sample.createCell(col++).setCellValue("样品类型\t均值(%)\t范围(%)\n血清(n=5)\t101\t84-105");
			sample.createCell(col++).setCellValue("样品类型\t1:2\t1:4\t1:8\n血清( (n=10)\t88-118%\t84-119%\t85-104%");
			sample.createCell(col++).setCellValue("Li Y, Wang Y, Hazen SL. C-reactive protein in cardiovascular disease: From biomarker to therapeutic target? Nature Reviews Cardiology. 2024;21(4):234-248.");

			// 隐藏sheet存放栏目清单与标签字典，对应列做下拉校验
			XSSFSheet hidden = wb.createSheet("opts");
			List<CategoryEntity> categories = listCategories();
			int r = 0;
			for (CategoryEntity c : categories) {
				hidden.createRow(r++).createCell(0).setCellValue(c.getCategoryTitle());
			}
			// 标签字典（label -> value），与文章编辑页标签选择器同源（中文/zh、英文/en）
			Map<String, String> tagDict = queryTagDict();
			int tr = 0;
			for (String label : tagDict.keySet()) {
				XSSFRow row = hidden.getRow(tr);
				if (row == null) {
					row = hidden.createRow(tr);
				}
				row.createCell(1).setCellValue(label);
				tr++;
			}
			wb.setSheetHidden(wb.getSheetIndex("opts"), true);
			DataValidationHelper helper = sheet.getDataValidationHelper();
			if (r > 0) {
				DataValidationConstraint constraint = helper.createFormulaListConstraint("opts!$A$1:$A$" + r);
				// 栏目列（第2列），数据区1..1000行
				CellRangeAddressList range = new CellRangeAddressList(1, 1000, 1, 1);
				DataValidation validation = helper.createValidation(constraint, range);
				validation.setSuppressDropDownArrow(true);
				validation.setShowErrorBox(true);
				sheet.addValidationData(validation);
			}
			if (tr > 0) {
				DataValidationConstraint tagConstraint = helper.createFormulaListConstraint("opts!$B$1:$B$" + tr);
				// 标签列（第3列）
				CellRangeAddressList tagRange = new CellRangeAddressList(1, 1000, 2, 2);
				DataValidation tagValidation = helper.createValidation(tagConstraint, tagRange);
				tagValidation.setSuppressDropDownArrow(true);
				tagValidation.setShowErrorBox(true);
				sheet.addValidationData(tagValidation);
			}

			// 列宽
			for (int i = 0; i < HEADER_MAP.size(); i++) {
				sheet.setColumnWidth(i, i == 4 ? 40 * 256 : 18 * 256);
			}

			response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
			response.setCharacterEncoding("UTF-8");
			String fileName = URLEncoder.encode("文章导入模板.xlsx", StandardCharsets.UTF_8).replace("+", "%20");
			response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + fileName);
			wb.write(response.getOutputStream());
		} catch (IOException e) {
			throw new RuntimeException("模板生成失败:" + e.getMessage(), e);
		}
	}

	@Override
	public Map<String, Object> importExcel(MultipartFile file, boolean generateStatic) {
		Map<String, Object> result = new LinkedHashMap<>();
		int created = 0, updated = 0, failed = 0;
		List<String> errors = new ArrayList<>();
		Set<String> affectedCategoryIds = new LinkedHashSet<>();

		// 规格模型（表单字段model名 -> 数据库列 的映射来源）
		ModelEntity model = modelBiz.lambdaQuery().eq(ModelEntity::getModelTableName, SPEC_TABLE).last("LIMIT 1").one();
		if (model == null) {
			throw new RuntimeException("产品规格模型不存在，无法导入规格字段");
		}

		// 栏目名称 -> 栏目实体（优先叶子列表栏目）
		Map<String, CategoryEntity> catMap = new HashMap<>();
		for (CategoryEntity c : listCategories()) {
			catMap.putIfAbsent(c.getCategoryTitle().trim(), c);
		}

		// 标签字典：中文标签名 -> 字典值（中文->zh、英文->en），导入时映射后存储
		Map<String, String> tagDict = queryTagDict();

		// 已有货号 -> 规格行(ID, LINK_ID)，用于按货号匹配更新
		Map<String, Map<String, Object>> specMap = new HashMap<>();
		for (Map<String, Object> row : jdbcTemplate.queryForList(
				"SELECT ID, LINK_ID, CATALOG_NO FROM " + SPEC_TABLE + " WHERE CATALOG_NO IS NOT NULL AND CATALOG_NO != ''")) {
			specMap.put(String.valueOf(row.get("CATALOG_NO")).trim(), row);
		}

		try (Workbook wb = WorkbookFactory.create(file.getInputStream())) {
			Sheet sheet = wb.getSheetAt(0);
			Row head = sheet.getRow(0);
			if (head == null) {
				throw new RuntimeException("缺少表头行");
			}
			// 表头名 -> 列号
			Map<String, Integer> colIdx = new HashMap<>();
			DataFormatter fmt = new DataFormatter();
			for (int i = 0; i < head.getLastCellNum(); i++) {
				String h = normalize(head.getCell(i));
				if (HEADER_MAP.containsKey(h)) {
					colIdx.put(h, i);
				}
			}
			for (String required : new String[]{"标题", "栏目", "货号"}) {
				if (!colIdx.containsKey(required)) {
					throw new RuntimeException("表头缺少必需列：" + required);
				}
			}

			FormulaEvaluator evaluator = wb.getCreationHelper().createFormulaEvaluator();
			for (int rn = 1; rn <= sheet.getLastRowNum(); rn++) {
				Row row = sheet.getRow(rn);
				if (row == null || isBlankRow(row)) {
					continue;
				}
				Map<String, String> vals = new HashMap<>();
				for (Map.Entry<String, Integer> e : colIdx.entrySet()) {
					Cell cell = row.getCell(e.getValue());
					String v = cell == null ? "" : fmt.formatCellValue(cell, evaluator).trim();
					vals.put(HEADER_MAP.get(e.getKey()), v);
				}
				int line = rn + 1;
				try {
					String title = vals.get("contentTitle");
					String catTitle = vals.get("categoryTitle");
					String catalogNo = vals.get("catalogNo");
					if (StrUtil.isBlank(title)) {
						throw new RuntimeException("标题为空");
					}
					if (StrUtil.isBlank(catTitle)) {
						throw new RuntimeException("栏目为空");
					}
					if (StrUtil.isBlank(catalogNo)) {
						throw new RuntimeException("货号为空");
					}
					CategoryEntity category = catMap.get(catTitle);
					if (category == null) {
						throw new RuntimeException("栏目不存在：" + catTitle);
					}

					// 文章：货号已有规格行则更新对应文章，否则新增
					Map<String, Object> specRow = specMap.get(catalogNo);
					ContentEntity article;
					boolean isNew = specRow == null;
					if (!isNew) {
						article = contentBiz.getById(String.valueOf(specRow.get("LINK_ID")));
						if (article == null) {
							throw new RuntimeException("货号已有规格数据但对应文章缺失，请先修复该数据");
						}
					} else {
						article = new ContentEntity();
						article.setContentDatetime(new Date());
						article.setContentDisplay("0");
						article.setContentAuthor("bioquanti");
						article.setContentSource("BioQuanti");
					}
					article.setContentTitle(title);
					article.setCategoryId(category.getId());
					// 标签：填字典标签名（中文/英文）或值（zh/en），多个逗号分隔，映射为字典值存储；
					// 留空时新增默认zh、更新保持原标签（见搜索语言隔离方案）
					String tagRaw = vals.get("contentTags");
					if (StrUtil.isNotBlank(tagRaw)) {
						article.setContentTags(mapTags(tagRaw, tagDict));
					} else if (isNew) {
						article.setContentTags("zh");
					}
					String img = vals.get("contentImg");
					// 缩略图按 category_img/litpic 同款JSON数组格式存储
					article.setContentImg(StrUtil.isBlank(img) ? "" : "[{\"url\":\"" + img + "\",\"name\":\"img\"}]");
					if (vals.get("contentDetails") != null) {
						article.setContentDetails(vals.get("contentDetails"));
					}
					contentBiz.saveOrUpdate(article);

					// 规格数据：键=模型字段model名（camelCase）；已有规格行走UPDATE（需带规格行id）
					// 必须用 CaseInsensitiveMap：ModelDataAop.formDataCheck 切面按该类型提取参数，普通 HashMap 会取到 null 导致 NPE
					Map<String, Object> params = new CaseInsensitiveMap<>();
					for (String key : new String[]{"catalogNo", "productCn", "productEn", "sampleName", "spec", "sensitivity",
							"detectRange", "stdTop", "stdDilutionSeries", "crossReactConc", "background", "calTable",
							"sampleTable", "precisionTable", "recoveryTable", "linearityTable", "literature"}) {
						String v = vals.get(key);
						if (StrUtil.isNotBlank(v)) {
							params.put(key, v);
						}
					}
					// 规格默认96T（说明书模板不变规格，未填时兜底，可按产品覆盖）
					if (StrUtil.isBlank(vals.get("spec"))) {
						params.put("spec", "96T");
					}
					params.put("linkId", article.getId());
					if (isNew) {
						modelDataBiz.saveDiyFormData(model, params);
					} else {
						params.put("id", String.valueOf(specRow.get("ID")));
						modelDataBiz.updateDiyFormData(model, params);
					}

					affectedCategoryIds.add(category.getId());
					if (isNew) {
						created++;
						// 新增规格行入缓存，避免同一Excel内重复货号重复插入；规格行ID由雪花算法在INSERT时生成，回查后登记
						List<Map<String, Object>> idRows = jdbcTemplate.queryForList(
								"SELECT ID FROM " + SPEC_TABLE + " WHERE LINK_ID = ? LIMIT 1", article.getId());
						if (!idRows.isEmpty()) {
							Map<String, Object> newSpec = new HashMap<>();
							newSpec.put("LINK_ID", article.getId());
							newSpec.put("ID", idRows.get(0).get("ID"));
							specMap.put(catalogNo, newSpec);
						}
					} else {
						updated++;
					}
				} catch (Exception rowEx) {
					failed++;
					String msg = "第" + line + "行: " + rowEx;
					LOG.warn("Excel导入行失败", rowEx);
					if (errors.size() < 50) {
						errors.add(msg);
					}
				}
			}
		} catch (IOException e) {
			throw new RuntimeException("Excel文件读取失败:" + e.getMessage(), e);
		}

		// 静态化：涉及栏目的列表页+文章详情页
		int staticFailed = 0;
		if (generateStatic && !affectedCategoryIds.isEmpty()) {
			for (String categoryId : affectedCategoryIds) {
				try {
					CategoryEntity column = categoryBiz.getById(categoryId);
					if (column == null || !CategoryTypeEnum.LIST.toString().equals(column.getCategoryType())) {
						continue;
					}
					ContentBean contentBean = new ContentBean();
					contentBean.setCategoryId(categoryId);
					contentBean.setCategoryType(column.getCategoryType());
					List<CategoryBean> articleIdList = contentBiz.queryIdsByCategoryIdForParser(contentBean);
					if (!articleIdList.isEmpty()) {
						// 列表页+详情页一并重新生成（与GeneraterAction同一套工具）
						if (StrUtil.isNotBlank(column.getCategoryListUrl())) {
							CmsParserUtil.generateList(column, articleIdList.size(), htmlDir);
						}
						CmsParserUtil.generateBasic(articleIdList, htmlDir, null);
					}
				} catch (Exception e) {
					staticFailed++;
					LOG.error("导入后静态化失败, categoryId={}", categoryId, e);
				}
			}
		}

		result.put("total", created + updated + failed);
		result.put("created", created);
		result.put("updated", updated);
		result.put("failed", failed);
		result.put("errors", errors);
		result.put("staticGenerated", generateStatic);
		result.put("staticFailed", staticFailed);
		return result;
	}

	/**
	 * 全部列表类型栏目（按层级路径排序，供模板下拉使用）
	 */
	private List<CategoryEntity> listCategories() {
		return categoryBiz.list(new LambdaQueryWrapper<CategoryEntity>()
				.eq(CategoryEntity::getCategoryType, CategoryTypeEnum.LIST.toString())
				.orderByAsc(CategoryEntity::getCategoryPath));
	}

	/**
	 * 文章标签字典（label -> value，如 中文->zh、英文->en），与文章编辑页标签选择器同源
	 */
	private Map<String, String> queryTagDict() {
		Map<String, String> dict = new LinkedHashMap<>();
		for (Map<String, Object> row : jdbcTemplate.queryForList(
				"SELECT DICT_LABEL, DICT_VALUE FROM mdiy_dict WHERE DICT_TYPE = ? AND IFNULL(DICT_ENABLE, 1) = 1 ORDER BY DICT_SORT, ID",
				TAG_DICT_TYPE)) {
			dict.put(String.valueOf(row.get("DICT_LABEL")), String.valueOf(row.get("DICT_VALUE")));
		}
		return dict;
	}

	/**
	 * 标签映射：字典标签名转字典值（中文->zh）；直接填值（zh）原样保留；多个用逗号/顿号分隔
	 */
	private String mapTags(String raw, Map<String, String> tagDict) {
		Set<String> tags = new LinkedHashSet<>();
		for (String part : raw.split("[,，、]")) {
			String s = part.trim();
			if (s.isEmpty()) {
				continue;
			}
			tags.add(tagDict.getOrDefault(s, s));
		}
		return String.join(",", tags);
	}

	/**
	 * 表头规范化：去空白、去必填标记*
	 */
	private String normalize(Cell cell) {
		if (cell == null) {
			return "";
		}
		DataFormatter fmt = new DataFormatter();
		return fmt.formatCellValue(cell).replace("*", "").replace(" ", "").replace("\u3000", "").trim();
	}

	/**
	 * 空行判断（全列无有效字符）
	 */
	private boolean isBlankRow(Row row) {
		DataFormatter fmt = new DataFormatter();
		for (int i = 0; i < row.getLastCellNum(); i++) {
			Cell c = row.getCell(i);
			if (c != null && !fmt.formatCellValue(c).trim().isEmpty()) {
				return false;
			}
		}
		return true;
	}
}
