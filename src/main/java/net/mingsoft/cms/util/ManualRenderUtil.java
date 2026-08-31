package net.mingsoft.cms.util;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import net.mingsoft.base.exception.BusinessException;
import org.apache.commons.lang3.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 说明书渲染工具：HTML模板占位符替换 + openhtmltopdf 生成PDF（全内存，不落盘）
 * @version
 * 版本号：1.0.0<br/>
 * 创建日期：2026-08-30<br/>
 */
public class ManualRenderUtil {

	/**
	 * 占位符语法 {{字段名}}（字段名与规格表列名一致，大写）
	 */
	private static final Pattern PLACEHOLDER = Pattern.compile("\\{\\{([A-Za-z0-9_]+)\\}\\}");

	/**
	 * 字体族名，模板CSS中使用 font-family:'NotoSansSC'
	 */
	public static final String FONT_FAMILY = "NotoSansSC";

	/**
	 * 提取classpath字体文件到临时文件（仅一次），openhtmltopdf按File注册字体
	 */
	private static volatile File fontFile;

	/**
	 * 占位符替换：值为null/空串渲染为"-"；值做HTML转义防止破坏模板结构
	 * 多行且含Tab/竖线分隔的值（编辑从Excel粘贴的表格数据）自动转为HTML表格
	 * @param templateHtml 模板HTML
	 * @param values 规格数据（key为列名，大小写不敏感）
	 * @return 替换后的HTML
	 */
	public static String substitute(String templateHtml, Map<String, Object> values) {
		if (StringUtils.isBlank(templateHtml)) {
			throw new BusinessException("模板内容为空");
		}
		Matcher matcher = PLACEHOLDER.matcher(templateHtml);
		StringBuilder sb = new StringBuilder();
		while (matcher.find()) {
			String key = matcher.group(1);
			String value = "-";
			if (values != null) {
				for (Map.Entry<String, Object> e : values.entrySet()) {
					if (key.equalsIgnoreCase(e.getKey()) && e.getValue() != null
							&& StringUtils.isNotBlank(String.valueOf(e.getValue()))) {
						value = String.valueOf(e.getValue());
						break;
					}
				}
			}
			matcher.appendReplacement(sb, Matcher.quoteReplacement(renderValue(value)));
		}
		matcher.appendTail(sb);
		return sb.toString();
	}

	/**
	 * 值渲染：表格样式的纯文本（多行+Tab/竖线分隔）转HTML表格（首行为表头），
	 * 其余按普通文本HTML转义。生成的表格带 bq-manual-tbl class，样式由模板CSS定义
	 */
	public static String renderValue(String value) {
		String rendered = escapeHtml(value);
		if (isTableText(value)) {
			return toHtmlTable(value);
		}
		return rendered;
	}

	/**
	 * 判断是否为表格文本：至少2行非空文本且各行含Tab或竖线分隔符
	 */
	private static boolean isTableText(String value) {
		if (value == null) {
			return false;
		}
		String[] lines = value.replace("\r\n", "\n").replace("\r", "\n").split("\n");
		int dataLines = 0;
		for (String line : lines) {
			if (line.trim().isEmpty()) {
				continue;
			}
			if (!line.contains("\t") && !line.contains("|")) {
				return false;
			}
			dataLines++;
		}
		return dataLines >= 2;
	}

	/**
	 * 表格文本转HTML表格：首行为表头th，其余为td；单元格内容做HTML转义
	 */
	private static String toHtmlTable(String value) {
		String[] lines = value.replace("\r\n", "\n").replace("\r", "\n").split("\n");
		StringBuilder sb = new StringBuilder("<table class=\"bq-manual-tbl\">");
		boolean header = true;
		for (String line : lines) {
			if (line.trim().isEmpty()) {
				continue;
			}
			// 行内双Tab清洗为单Tab，避免空单元格
			String[] cells = line.replace("\t\t", "\t").split("\t");
			if (cells.length == 1) {
				cells = line.split("\\|");
			}
			sb.append("<tr>");
			for (String cell : cells) {
				String text = escapeHtml(cell.trim());
				sb.append(header ? "<th>" : "<td>").append(text).append(header ? "</th>" : "</td>");
			}
			sb.append("</tr>");
			header = false;
		}
		return sb.append("</table>").toString();
	}

	/**
	 * HTML转义（防止规格值中含特殊字符破坏PDF渲染）
	 */
	public static String escapeHtml(String text) {
		if (text == null) {
			return "";
		}
		return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
	}

	/**
	 * HTML渲染为PDF字节流（全内存，不产生临时文件）
	 * 用户上传的模板多为非闭合HTML标签（meta/img/br等），openhtmltopdf要求严格XHTML，
	 * 故先经jsoup解析并按xml语法规范化，再转W3C DOM渲染
	 * @param html 已完成占位符替换的HTML
	 * @return PDF字节
	 */
	public static byte[] htmlToPdf(String html) {
		return htmlToPdf(html, null);
	}

	/**
	 * HTML渲染为PDF字节流（全内存，不产生临时文件）
	 * @param html 已完成占位符替换的HTML
	 * @param baseUri 基准URI（模板文件所在目录的file: URL），用于解析模板中的相对图片路径，可为null
	 * @return PDF字节
	 */
	public static byte[] htmlToPdf(String html, String baseUri) {
		try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
			org.jsoup.nodes.Document jsoupDoc = org.jsoup.Jsoup.parse(html);
			jsoupDoc.outputSettings()
					.syntax(org.jsoup.nodes.Document.OutputSettings.Syntax.xml)
					.charset(StandardCharsets.UTF_8)
					.prettyPrint(false);
			org.w3c.dom.Document w3cDoc = new org.jsoup.helper.W3CDom().fromJsoup(jsoupDoc);
			PdfRendererBuilder builder = new PdfRendererBuilder();
			builder.useFastMode();
			builder.withW3cDocument(w3cDoc, baseUri);
			builder.useFont(getFontFile(), FONT_FAMILY);
			builder.toStream(out);
			builder.run();
			return out.toByteArray();
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new BusinessException("说明书PDF生成失败:" + e.getMessage());
		}
	}

	/**
	 * classpath字体解压到临时文件（双检锁，仅首次）
	 */
	private static File getFontFile() {
		if (fontFile == null) {
			synchronized (ManualRenderUtil.class) {
				if (fontFile == null) {
					try {
						File tmp = File.createTempFile("nsc-font-", ".ttf");
						tmp.deleteOnExit();
						try (InputStream in = ManualRenderUtil.class.getResourceAsStream("/fonts/NotoSansSC-Regular.ttf");
					     FileOutputStream fos = new FileOutputStream(tmp)) {
						if (in == null) {
							throw new BusinessException("中文字体文件缺失:resources/fonts/NotoSansSC-Regular.ttf（须为TrueType轮廓，CFF轮廓的otf不被openhtmltopdf支持）");
						}
							in.transferTo(fos);
						}
						fontFile = tmp;
					} catch (BusinessException e) {
						throw e;
					} catch (Exception e) {
						throw new BusinessException("字体文件加载失败:" + e.getMessage());
					}
				}
			}
		}
		return fontFile;
	}
}
