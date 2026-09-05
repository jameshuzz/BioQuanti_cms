package net.mingsoft.cms.util;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import net.mingsoft.base.exception.BusinessException;
import org.apache.commons.lang3.StringUtils;

import javax.imageio.ImageIO;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 说明书渲染工具：HTML模板占位符替换 + openhtmltopdf 生成PDF（全内存，不落盘）
 * 占位符语法（字段名与规格表列名一致，大小写不敏感）：
 *   {{X}}         整值渲染：表格文本（多行Tab/竖线分隔）转完整HTML表格（首行th），其余转义文本
 *   {{X_ROWS}}    数据行模式：表格文本只转 &lt;tr&gt;&lt;td&gt; 序列（表头由模板固定），配合模板中写死的表头使用
 *   {{X_LINES}}   行列表模式：每行转 &lt;li&gt;，配合模板 &lt;ol&gt;/&lt;ul&gt; 使用（如参考文献）
 *   {{X_CHART}}   图表模式：按X列表格数据生成标准曲线PNG（log-x折线图），输出 &lt;img&gt; data URI
 * @version
 * 版本号：1.1.0<br/>
 * 创建日期：2026-08-30<br/>
 * 历史修订：2026-09-05 增加_ROWS/_LINES/_CHART渲染模式与字段默认值支持
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
	 * 占位符替换（无默认值）：值为null/空串渲染为"-"
	 * @see #substitute(String, Map, Map)
	 */
	public static String substitute(String templateHtml, Map<String, Object> values) {
		return substitute(templateHtml, values, null);
	}

	/**
	 * 占位符替换：值做HTML转义防止破坏模板结构
	 * 取值优先级：完整token名（如CAL_TABLE_ROWS本身是列名）> 去后缀列名 > 默认值 > "-"
	 * @param templateHtml 模板HTML
	 * @param values 规格数据（key为列名，大小写不敏感）
	 * @param defaults 字段默认值（如 SPEC->96T；数据未填时兜底，大小写不敏感），可为null
	 * @return 替换后的HTML
	 */
	public static String substitute(String templateHtml, Map<String, Object> values, Map<String, String> defaults) {
		if (StringUtils.isBlank(templateHtml)) {
			throw new BusinessException("模板内容为空");
		}
		Matcher matcher = PLACEHOLDER.matcher(templateHtml);
		StringBuilder sb = new StringBuilder();
		while (matcher.find()) {
			String token = matcher.group(1);
			// 解析渲染模式后缀
			String mode = "VALUE";
			String col = token;
			if (token.endsWith("_ROWS") && token.length() > 5) {
				mode = "ROWS";
				col = token.substring(0, token.length() - 5);
			} else if (token.endsWith("_LINES") && token.length() > 6) {
				mode = "LINES";
				col = token.substring(0, token.length() - 6);
			} else if (token.endsWith("_CHART") && token.length() > 6) {
				mode = "CHART";
				col = token.substring(0, token.length() - 6);
			}
			// 取值：先按完整token找列，再按去后缀列名找，再走默认值
			String value = lookupValue(values, token);
			if (value == null) {
				value = lookupValue(values, col);
			}
			if (value == null && defaults != null) {
				for (Map.Entry<String, String> d : defaults.entrySet()) {
					if (col.equalsIgnoreCase(d.getKey()) && StringUtils.isNotBlank(d.getValue())) {
						value = d.getValue();
						break;
					}
				}
			}
			if (value == null) {
				value = "-";
			}
			String rendered;
			switch (mode) {
				case "ROWS":
					rendered = toRows(value);
					break;
				case "LINES":
					rendered = toLines(value);
					break;
				case "CHART":
					rendered = toChartImg(value);
					break;
				default:
					rendered = renderValue(value);
			}
			matcher.appendReplacement(sb, Matcher.quoteReplacement(rendered));
		}
		matcher.appendTail(sb);
		return sb.toString();
	}

	/**
	 * 大小写不敏感取非空值，找不到返回null
	 */
	private static String lookupValue(Map<String, Object> values, String key) {
		if (values == null || key == null) {
			return null;
		}
		for (Map.Entry<String, Object> e : values.entrySet()) {
			if (key.equalsIgnoreCase(e.getKey()) && e.getValue() != null
					&& StringUtils.isNotBlank(String.valueOf(e.getValue()))) {
				return String.valueOf(e.getValue());
			}
		}
		return null;
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
	 * 数据行模式：表格文本转tr/td序列（不包table标签），配合模板中写死的表头使用。
	 * 约定首行为表头（与编辑表格/Excel粘贴格式一致），渲染时跳过；
	 * 非表格文本（含未填占位"-"）返回空串，避免破坏外层表格结构
	 */
	private static String toRows(String value) {
		if (!isTableText(value)) {
			return "";
		}
		String[] lines = value.replace("\r\n", "\n").replace("\r", "\n").split("\n");
		StringBuilder sb = new StringBuilder();
		boolean header = true;
		for (String line : lines) {
			if (line.trim().isEmpty()) {
				continue;
			}
			// 首行为表头，PDF模板已固定，跳过
			if (header) {
				header = false;
				continue;
			}
			String[] cells = line.replace("\t\t", "\t").split("\t");
			if (cells.length == 1) {
				cells = line.split("\\|");
			}
			sb.append("<tr>");
			for (String cell : cells) {
				sb.append("<td>").append(escapeHtml(cell.trim())).append("</td>");
			}
			sb.append("</tr>");
		}
		return sb.toString();
	}

	/**
	 * 行列表模式：每行非空文本转一个li，配合模板ol/ul使用（如参考文献）；
	 * 未填（占位"-"或空）返回空串
	 */
	private static String toLines(String value) {
		if (value == null || "-".equals(value.trim())) {
			return "";
		}
		String[] lines = value.replace("\r\n", "\n").replace("\r", "\n").split("\n");
		StringBuilder sb = new StringBuilder();
		for (String line : lines) {
			if (line.trim().isEmpty()) {
				continue;
			}
			// 去掉手工编号前缀（1. 2. （1）等），模板ol自带编号
			String text = line.trim().replaceFirst("^(\\d+\\s*[.、）)]|[（(]\\d+[）)])\\s*", "");
			sb.append("<li>").append(escapeHtml(text)).append("</li>");
		}
		return sb.toString();
	}

	/**
	 * 图表模式：按表格数据生成标准曲线图（log-x浓度轴 + 线性OD轴折线），
	 * 输出内嵌data URI的img标签；无有效数据返回空串
	 * 数据格式：首行为表头（跳过），首列=浓度，末列优先取Corrected列（倒数第1列），
	 * 无则取Average（倒数第2列），浓度&lt;=0的点跳过（log轴无意义）
	 */
	private static String toChartImg(String value) {
		double[][] points = parseCurvePoints(value);
		if (points.length < 2) {
			return "";
		}
		try {
			byte[] png = renderStdCurvePng(points);
			String base64 = Base64.getEncoder().encodeToString(png);
			return "<img style=\"width:250px;\" src=\"data:image/png;base64," + base64 + "\"/>";
		} catch (Exception e) {
			return "";
		}
	}

	/**
	 * 解析标曲数据点：[浓度, OD]，按浓度升序
	 */
	private static double[][] parseCurvePoints(String value) {
		if (value == null) {
			return new double[0][];
		}
		String[] lines = value.replace("\r\n", "\n").replace("\r", "\n").split("\n");
		List<double[]> pts = new ArrayList<>();
		for (String line : lines) {
			if (line.trim().isEmpty()) {
				continue;
			}
			String[] cells = line.replace("\t\t", "\t").split("\t");
			if (cells.length == 1) {
				cells = line.split("\\|");
			}
			if (cells.length < 2) {
				continue;
			}
			try {
				double x = Double.parseDouble(cells[0].trim());
				// y优先取末列（Corrected），无则倒数第2列（Average）
				double y = parseDoubleSafe(cells[cells.length - 1]);
				if (Double.isNaN(y) && cells.length >= 3) {
					y = parseDoubleSafe(cells[cells.length - 2]);
				}
				if (x > 0 && !Double.isNaN(y)) {
					pts.add(new double[]{x, y});
				}
			} catch (NumberFormatException ignore) {
				// 表头行等非数字行跳过
			}
		}
		pts.sort((a, b) -> Double.compare(a[0], b[0]));
		return pts.toArray(new double[0][]);
	}

	private static double parseDoubleSafe(String s) {
		try {
			return Double.parseDouble(s.trim());
		} catch (Exception e) {
			return Double.NaN;
		}
	}

	/**
	 * Java2D绘制标准曲线PNG（无第三方依赖）：log-x折线图，含坐标轴/刻度/数据点
	 */
	private static byte[] renderStdCurvePng(double[][] points) throws Exception {
		int w = 320, h = 250;
		int left = 48, right = 12, top = 14, bottom = 34;
		BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = img.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, w, h);
		g.setColor(Color.BLACK);
		Font font = new Font("SansSerif", Font.PLAIN, 10);
		g.setFont(font);

		double minX = points[0][0], maxX = points[points.length - 1][0];
		double maxY = 0;
		for (double[] p : points) {
			maxY = Math.max(maxY, p[1]);
		}
		maxY = Math.ceil(maxY * 1.1 * 10) / 10.0;
		if (maxY <= 0) {
			maxY = 1;
		}
		double lgMin = Math.log10(minX), lgMax = Math.log10(maxX);
		int plotW = w - left - right, plotH = h - top - bottom;

		// 网格与坐标刻度
		g.setColor(new Color(220, 220, 220));
		int ySteps = 5;
		for (int i = 0; i <= ySteps; i++) {
			int y = top + plotH - (int) Math.round(plotH * i / (double) ySteps);
			g.drawLine(left, y, left + plotW, y);
			g.setColor(Color.BLACK);
			String label = trimNum(maxY * i / ySteps);
			g.drawString(label, left - 34, y + 4);
			g.setColor(new Color(220, 220, 220));
		}
		// x刻度用实际数据浓度值，密集时隔一个标注
		g.setColor(Color.BLACK);
		int step = points.length > 7 ? 2 : 1;
		for (int i = 0; i < points.length; i += step) {
			int x = left + (int) Math.round(plotW * (Math.log10(points[i][0]) - lgMin) / (lgMax - lgMin));
			g.drawLine(x, top + plotH, x, top + plotH + 3);
			String label = trimNum(points[i][0]);
			int tw = g.getFontMetrics().stringWidth(label);
			g.drawString(label, x - tw / 2, top + plotH + 15);
		}
		// 坐标轴
		g.setColor(Color.BLACK);
		g.setStroke(new BasicStroke(1.2f));
		g.drawLine(left, top, left, top + plotH);
		g.drawLine(left, top + plotH, left + plotW, top + plotH);
		g.drawString("OD450", 4, top + 10);
		String unit = "pg/mL";
		g.drawString(unit, left + plotW - g.getFontMetrics().stringWidth(unit), top + plotH + 27);

		// 折线与数据点
		g.setColor(new Color(26, 92, 158));
		g.setStroke(new BasicStroke(1.6f));
		for (int i = 0; i < points.length; i++) {
			int x = left + (int) Math.round(plotW * (Math.log10(points[i][0]) - lgMin) / (lgMax - lgMin));
			int y = top + plotH - (int) Math.round(plotH * points[i][1] / maxY);
			if (i == 0) {
				g.drawLine(x, y, x, y);
			} else {
				int px = left + (int) Math.round(plotW * (Math.log10(points[i - 1][0]) - lgMin) / (lgMax - lgMin));
				int py = top + plotH - (int) Math.round(plotH * points[i - 1][1] / maxY);
				g.drawLine(px, py, x, y);
			}
		}
		for (double[] p : points) {
			int x = left + (int) Math.round(plotW * (Math.log10(p[0]) - lgMin) / (lgMax - lgMin));
			int y = top + plotH - (int) Math.round(plotH * p[1] / maxY);
			g.fill(new Ellipse2D.Double(x - 2.5, y - 2.5, 5, 5));
		}
		g.dispose();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ImageIO.write(img, "png", out);
		return out.toByteArray();
	}

	/**
	 * 数字标签修剪：整数去小数点，小数保留最多2位
	 */
	private static String trimNum(double d) {
		if (d == Math.floor(d)) {
			return String.valueOf((long) d);
		}
		String s = String.format("%.2f", d);
		if (s.endsWith("0")) {
			s = s.substring(0, s.length() - 1);
		}
		return s;
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
