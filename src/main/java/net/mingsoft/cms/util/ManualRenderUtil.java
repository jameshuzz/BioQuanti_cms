package net.mingsoft.cms.util;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.openhtmltopdf.outputdevice.helper.BaseRendererBuilder.FontStyle;
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
 *   {{X_CHART}}   图表模式：按X列表格数据生成标准曲线PNG（线性浓度轴+4PL拟合曲线），输出 &lt;img&gt; data URI
 * @version
 * 版本号：1.2.0<br/>
 * 创建日期：2026-08-30<br/>
 * 历史修订：2026-09-05 增加_ROWS/_LINES/_CHART渲染模式与字段默认值支持<br/>
 * 历史修订：2026-11-17 图表改为客户规格：4PL拟合曲线、X轴线性nice刻度、Y轴0.5步进、动态浓度单位
 */
public class ManualRenderUtil {

	/**
	 * 占位符语法 {{字段名}}（字段名与规格表列名一致，大写）
	 */
	private static final Pattern PLACEHOLDER = Pattern.compile("\\{\\{([A-Za-z0-9_]+)\\}\\}");

	/**
	 * 默认字体族名（宋体风格衬线），模板CSS中使用 font-family:'NotoSerifSC'
	 */
	public static final String FONT_FAMILY = "NotoSerifSC";

	/**
	 * 提取classpath字体文件到临时文件（仅一次），openhtmltopdf按File注册字体
	 */
	private static final java.util.concurrent.ConcurrentHashMap<String, File> FONT_CACHE = new java.util.concurrent.ConcurrentHashMap<>();

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
		// 竞争法标记：标曲表不扣空白（无Corrected列），图表用Average列且0浓度点(B0)参与拟合
		boolean noCorr = templateHtml.contains("<!--NO_CORRECTED-->");
		// 条件块：<!--IF:字段=值-->内容<!--ENDIF-->（数据值匹配则保留内容，否则整块删除，用于单抗/多抗等文案分支）
		templateHtml = renderCondBlocks(templateHtml, values);
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
			// 图表轴单位：优先取 <列名>_UNIT（如CAL_TABLE_UNIT），回退CAL_UNIT（渲染时由STD_TOP提取）
			String chartUnit = null;
			if ("CHART".equals(mode)) {
				chartUnit = lookupValue(values, col + "_UNIT");
				if (chartUnit == null) {
					chartUnit = lookupValue(values, "CAL_UNIT");
				}
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
					rendered = toRows(value, noCorr);
					break;
				case "LINES":
					rendered = toLines(value);
					break;
				case "CHART":
					rendered = toChartImg(value, chartUnit, noCorr);
					break;
				default:
					rendered = renderValue(value);
			}
			matcher.appendReplacement(sb, Matcher.quoteReplacement(rendered));
		}
		matcher.appendTail(sb);
		String result = sb.toString();
		if (noCorr) {
			result = result.replace("<!--NO_CORRECTED-->", "");
		}
		return result;
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
	 * 条件块渲染：<!--IF:字段=值-->内容<!--ENDIF-->
	 * 数据值与块声明值忽略大小写匹配则保留内容（去除标记），否则整块删除；支持同一字段多个块、模板中出现多组块
	 */
	private static String renderCondBlocks(String html, Map<String, Object> values) {
		if (html == null || !html.contains("<!--IF:")) {
			return html;
		}
		Pattern cond = Pattern.compile("<!--IF:([A-Za-z0-9_]+)=([^>]*)-->(.*?)<!--ENDIF-->", Pattern.DOTALL);
		Matcher m = cond.matcher(html);
		StringBuilder sb = new StringBuilder();
		while (m.find()) {
			String field = m.group(1);
			String expect = m.group(2).trim();
			String actual = lookupValue(values, field);
			boolean keep = actual != null && actual.trim().equalsIgnoreCase(expect);
			m.appendReplacement(sb, Matcher.quoteReplacement(keep ? m.group(3) : ""));
		}
		m.appendTail(sb);
		return sb.toString();
	}

	/**
	 * 数据行模式：表格文本转tr/td序列（不包table标签），配合模板中写死的表头使用。
	 * 约定首行为表头（与编辑表格/Excel粘贴格式一致），渲染时跳过；
	 * noCorr=true（竞争法）：数据行多于4列时裁掉末尾Corrected列，输出前4列；
	 * 非表格文本（含未填占位"-"）返回空串，避免破坏外层表格结构
	 */
	private static String toRows(String value, boolean noCorr) {
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
			if (noCorr && cells.length > 4) {
				String[] c4 = new String[4];
				System.arraycopy(cells, 0, c4, 0, 4);
				cells = c4;
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
	 * 无则取Average（倒数第2列），浓度&lt;0的点跳过
	 */
	private static String toChartImg(String value, String unit, boolean noCorr) {
		double[][] points = parseCurvePoints(value, noCorr);
		if (points.length < 2) {
			return "";
		}
		try {
			byte[] png = renderStdCurvePng(points, unit, noCorr);
			String base64 = Base64.getEncoder().encodeToString(png);
			return "<img style=\"width:270px;\" src=\"data:image/png;base64," + base64 + "\"/>";
		} catch (Exception e) {
			return "";
		}
	}

	/**
	 * 解析标曲数据点：[浓度, OD]，按浓度升序
	 * noCorr=true（竞争法）：y固定取倒数第2列（Average，不扣空白），0浓度点保留参与拟合
	 */
	private static double[][] parseCurvePoints(String value, boolean noCorr) {
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
				double y;
				if (noCorr) {
					// 竞争法：不扣空白，y取Average列（倒数第2列）；数据仅2列时取末列
					y = cells.length >= 3 ? parseDoubleSafe(cells[cells.length - 2])
							: parseDoubleSafe(cells[cells.length - 1]);
				} else {
					// y优先取末列（Corrected），无则倒数第2列（Average）
					y = parseDoubleSafe(cells[cells.length - 1]);
					if (Double.isNaN(y) && cells.length >= 3) {
						y = parseDoubleSafe(cells[cells.length - 2]);
					}
				}
				if (x >= 0 && !Double.isNaN(y)) {
					// x=0点：正比法=Blank(不参与拟合)；竞争法=B0点(参与拟合)，由noCorr区分
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
	 * Java2D绘制标准曲线PNG（无第三方依赖）：
	 * X轴=线性浓度轴（上限取不小于浓度最大值的最小"整齐"值，4~5个均匀整刻度），
	 * Y轴=OD轴（0.5步进刻度，上限=数据最大值向上取整到0.5），
	 * 曲线=4PL(Logistic四参数)拟合平滑曲线（拟合失败回退折线），
	 * 散点=全部数据点；noCorr=false时x=0为Blank点不参与拟合，true（竞争法）时0浓度B0点参与拟合
	 */
	private static byte[] renderStdCurvePng(double[][] points, String unit, boolean noCorr) throws Exception {
		int w = 320, h = 250;
		int left = 48, right = 14, top = 14, bottom = 36;
		BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = img.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, w, h);
		g.setColor(Color.BLACK);
		Font font = new Font("SansSerif", Font.PLAIN, 10);
		g.setFont(font);

		// 拟合点集合：正比法排除x=0的Blank点；竞争法(noCorr=true)0浓度B0点参与拟合
		List<double[]> fitList = new ArrayList<>();
		for (double[] p : points) {
			if (p[0] > 0 || noCorr) {
				fitList.add(p);
			}
		}
		double[][] fitPts = fitList.toArray(new double[0][]);
		double xMax = fitPts[fitPts.length - 1][0];
		double yMax = 0;
		for (double[] p : points) {
			yMax = Math.max(yMax, p[1]);
		}

		// X轴：线性，nice上限+4~5个均匀整刻度
		double[] xa = niceXAxis(xMax);
		double xUpper = xa[0], xStep = xa[1];
		// Y轴：0.5步进，上限=数据最大值向上取整到0.5（不低于1.0）
		double yUpper = Math.max(1.0, Math.ceil(yMax / 0.5 - 1e-9) * 0.5);
		int yTicks = (int) Math.round(yUpper / 0.5);
		int xTicks = (int) Math.round(xUpper / xStep);
		int plotW = w - left - right, plotH = h - top - bottom;

		// 虚线网格（横纵，浅灰）
		g.setColor(new Color(150, 150, 150));
		g.setStroke(new BasicStroke(0.6f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
				10f, new float[]{1.5f, 1.5f}, 0f));
		for (int i = 0; i <= yTicks; i++) {
			double yv = 0.5 * i;
			int y = top + plotH - (int) Math.round(plotH * yv / yUpper);
			g.drawLine(left, y, left + plotW, y);
		}
		for (int i = 0; i <= xTicks; i++) {
			double xv = xStep * i;
			int x = left + (int) Math.round(plotW * xv / xUpper);
			g.drawLine(x, top, x, top + plotH);
		}
		// 刻度标签与刻度线
		g.setColor(Color.BLACK);
		for (int i = 0; i <= yTicks; i++) {
			double yv = 0.5 * i;
			int y = top + plotH - (int) Math.round(plotH * yv / yUpper);
			g.drawLine(left - 3, y, left, y);
			String label = trimNum(yv);
			g.drawString(label, left - 8 - g.getFontMetrics().stringWidth(label), y + 4);
		}
		for (int i = 0; i <= xTicks; i++) {
			double xv = xStep * i;
			int x = left + (int) Math.round(plotW * xv / xUpper);
			g.drawLine(x, top + plotH, x, top + plotH + 3);
			String label = trimNum(xv);
			int tw = g.getFontMetrics().stringWidth(label);
			// 0标签右移防出界，末标签左移防出界
			int lx = x - tw / 2;
			if (i == 0) {
				lx = x;
			} else if (i == xTicks) {
				lx = x - tw;
			}
			g.drawString(label, lx, top + plotH + 15);
		}
		// 坐标轴
		g.setColor(Color.BLACK);
		g.setStroke(new BasicStroke(1.2f));
		g.drawLine(left, top, left, top + plotH);
		g.drawLine(left, top + plotH, left + plotW, top + plotH);
		// 轴标签：Y轴OD450纵向居中，X轴浓度单位底部居中
		Graphics2D gy = (Graphics2D) g.create();
		gy.rotate(-Math.PI / 2, 10, top + plotH / 2.0);
		gy.drawString("OD450", 10 - gy.getFontMetrics().stringWidth("OD450") / 2, top + plotH / 2 + 4);
		gy.dispose();
		String unitText = (unit == null || unit.trim().isEmpty()) ? "pg/mL" : unit.trim();
		g.drawString(unitText, left + plotW / 2 - g.getFontMetrics().stringWidth(unitText) / 2, top + plotH + 28);

		// 4PL拟合平滑曲线（红色），拟合失败回退折线
		double[] params = fit4PL(fitPts);
		g.setColor(new Color(192, 0, 0));
		g.setStroke(new BasicStroke(1.5f));
		if (params != null) {
			double xMin = fitPts[0][0];
			int prevX = -1, prevY = -1;
			for (int i = 0; i <= 300; i++) {
				double xv = xMin + (xMax - xMin) * i / 300.0;
				double yv = model4pl(xv, params);
				if (Double.isNaN(yv)) {
					continue;
				}
				yv = Math.max(0, Math.min(yv, yUpper));
				int px = left + (int) Math.round(plotW * xv / xUpper);
				int py = top + plotH - (int) Math.round(plotH * yv / yUpper);
				if (i > 0) {
					g.drawLine(prevX, prevY, px, py);
				}
				prevX = px;
				prevY = py;
			}
		} else {
			for (int i = 0; i < points.length; i++) {
				int x = left + (int) Math.round(plotW * points[i][0] / xUpper);
				int y = top + plotH - (int) Math.round(plotH * points[i][1] / yUpper);
				if (i > 0) {
					int px = left + (int) Math.round(plotW * points[i - 1][0] / xUpper);
					int py = top + plotH - (int) Math.round(plotH * points[i - 1][1] / yUpper);
					g.drawLine(px, py, x, y);
				}
			}
		}
		// 数据散点（红色实心，含原点Blank点）
		for (double[] p : points) {
			int x = left + (int) Math.round(plotW * p[0] / xUpper);
			int y = top + plotH - (int) Math.round(plotH * p[1] / yUpper);
			g.fill(new Ellipse2D.Double(x - 2.5, y - 2.5, 5, 5));
		}
		g.dispose();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ImageIO.write(img, "png", out);
		return out.toByteArray();
	}

	/**
	 * 4PL(Logistic四参数)模型：y = d + (a-d) / (1 + (x/c)^b)
	 * p[0]=a下渐近线(低浓度响应) p[1]=b斜率因子 p[2]=c EC50 p[3]=d上渐近线
	 */
	private static double model4pl(double x, double[] p) {
		return p[3] + (p[0] - p[3]) / (1.0 + Math.pow(x / p[2], p[1]));
	}

	private static double sse4pl(double[][] pts, double[] p) {
		double s = 0;
		for (double[] pt : pts) {
			double e = pt[1] - model4pl(pt[0], p);
			s += e * e;
		}
		return s;
	}

	/**
	 * 4PL拟合（Levenberg-Marquardt + 数值雅可比，无第三方依赖）
	 * 点数<4或迭代不收敛返回null（调用方回退折线）
	 */
	private static double[] fit4PL(double[][] pts) {
		int n = pts.length;
		if (n < 4) {
			return null;
		}
		double minY = Double.MAX_VALUE, maxY = -Double.MAX_VALUE, geoLog = 0;
		int posN = 0;
		for (double[] pt : pts) {
			minY = Math.min(minY, pt[1]);
			maxY = Math.max(maxY, pt[1]);
			// EC50初值用正浓度点的几何均值（竞争法含x=0的B0点，log10(0)无意义需排除）
			if (pt[0] > 0) {
				geoLog += Math.log10(pt[0]);
				posN++;
			}
		}
		double span = maxY - minY;
		if (span < 1e-9 || minY < 0 || posN == 0) {
			return null;
		}
		// 初值按曲线方向自适应：模型x→0时y→a(低浓度渐近线)、x→∞时y→d(高浓度渐近线)。
		// 正比曲线(浓度升序首点y<末点y)：a取小值d取大值；反比竞争法曲线：a取大值d取小值
		boolean decreasing = pts[0][1] > pts[n - 1][1];
		double a0 = decreasing ? maxY + span * 0.05 : minY - span * 0.05;
		double d0 = decreasing ? minY - span * 0.05 : maxY + span * 0.05;
		double[] p = {a0, 1.0, Math.pow(10, geoLog / posN), d0};
		if (p[2] <= 0) {
			return null;
		}
		double lambda = 1e-3;
		double sse = sse4pl(pts, p);
		for (int iter = 0; iter < 200; iter++) {
			double[][] J = new double[n][4];
			double[] r = new double[n];
			for (int i = 0; i < n; i++) {
				r[i] = pts[i][1] - model4pl(pts[i][0], p);
				for (int j = 0; j < 4; j++) {
					double eps = Math.max(1e-9, Math.abs(p[j]) * 1e-6);
					double[] p2 = p.clone();
					p2[j] += eps;
					J[i][j] = (model4pl(pts[i][0], p2) - model4pl(pts[i][0], p)) / eps;
				}
			}
			double[][] N = new double[4][4];
			double[] gvec = new double[4];
			for (int i = 0; i < n; i++) {
				for (int j = 0; j < 4; j++) {
					gvec[j] += J[i][j] * r[i];
					for (int k = 0; k < 4; k++) {
						N[j][k] += J[i][j] * J[i][k];
					}
				}
			}
			for (int j = 0; j < 4; j++) {
				N[j][j] *= (1.0 + lambda);
			}
			double[] delta = solve4(N, gvec);
			if (delta == null) {
				return null;
			}
			double[] pNew = {p[0] + delta[0], p[1] + delta[1], p[2] + delta[2], p[3] + delta[3]};
			if (pNew[1] <= 0 || pNew[2] <= 0 || Double.isNaN(pNew[0]) || Double.isNaN(pNew[3])) {
				lambda *= 10;
				if (lambda > 1e12) {
					return null;
				}
				continue;
			}
			double sseNew = sse4pl(pts, pNew);
			if (sseNew < sse) {
				double improve = sse - sseNew;
				p = pNew;
				sse = sseNew;
				lambda = Math.max(lambda / 3, 1e-14);
				if (improve < 1e-14) {
					break;
				}
			} else {
				lambda *= 10;
				if (lambda > 1e12) {
					break;
				}
			}
		}
		if (Double.isNaN(p[0]) || Double.isNaN(p[3]) || p[1] <= 0 || p[1] > 1000 || p[2] <= 0) {
			return null;
		}
		return p;
	}

	/**
	 * 解4x4线性方程组（高斯消元+部分主元），奇异返回null
	 */
	private static double[] solve4(double[][] a, double[] b) {
		double[][] m = new double[4][5];
		for (int i = 0; i < 4; i++) {
			System.arraycopy(a[i], 0, m[i], 0, 4);
			m[i][4] = b[i];
		}
		for (int col = 0; col < 4; col++) {
			int piv = col;
			for (int row = col + 1; row < 4; row++) {
				if (Math.abs(m[row][col]) > Math.abs(m[piv][col])) {
					piv = row;
				}
			}
			if (Math.abs(m[piv][col]) < 1e-14) {
				return null;
			}
			double[] tmp = m[col];
			m[col] = m[piv];
			m[piv] = tmp;
			for (int row = 0; row < 4; row++) {
				if (row == col) {
					continue;
				}
				double f = m[row][col] / m[col][col];
				for (int k = col; k <= 4; k++) {
					m[row][k] -= f * m[col][k];
				}
			}
		}
		double[] x = new double[4];
		for (int i = 0; i < 4; i++) {
			x[i] = m[i][4] / m[i][i];
		}
		return x;
	}

	/**
	 * X轴nice刻度：上限=不小于浓度最大值的最小"整齐"值（1/2/2.5/5×10^n），刻度段数4~5
	 * 返回{上限, 步长}；无解时放宽为均分5段
	 */
	private static double[] niceXAxis(double dataMax) {
		if (dataMax <= 0) {
			return new double[]{1, 0.25};
		}
		double[] best = null;
		int exp = (int) Math.floor(Math.log10(dataMax));
		for (int e = exp - 2; e <= exp + 1; e++) {
			double base = Math.pow(10, e);
			for (double m : new double[]{1, 2, 2.5, 5}) {
				double step = m * base;
				double upper = Math.ceil(dataMax / step - 1e-9) * step;
				int seg = (int) Math.round(upper / step);
				if (seg < 4 || seg > 5) {
					continue;
				}
				if (best == null || upper < best[0] - 1e-9) {
					best = new double[]{upper, step};
				}
			}
		}
		if (best == null) {
			double upper = Math.ceil(dataMax / 5.0) * 5.0;
			return new double[]{upper, upper / 5};
		}
		return best;
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
			// 注册常规/粗体两个字重，同族名下CSS font-weight:bold 才会生效
			// NotoSansSC（黑体风格）与NotoSerifSC（宋体风格，对应Word正文）两族都注册，模板按需选用
			builder.useFont(getFontFile("/fonts/NotoSansSC-Regular.ttf"), "NotoSansSC", 400, FontStyle.NORMAL, true);
			builder.useFont(getFontFile("/fonts/NotoSansSC-Bold.ttf"), "NotoSansSC", 700, FontStyle.NORMAL, true);
			builder.useFont(getFontFile("/fonts/NotoSerifSC-Regular.ttf"), FONT_FAMILY, 400, FontStyle.NORMAL, true);
			builder.useFont(getFontFile("/fonts/NotoSerifSC-Bold.ttf"), FONT_FAMILY, 700, FontStyle.NORMAL, true);
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
	 * classpath字体解压到临时文件（按资源路径缓存），openhtmltopdf按File注册字体
	 */
	private static File getFontFile(String resourcePath) {
		return FONT_CACHE.computeIfAbsent(resourcePath, path -> {
			try {
				File tmp = File.createTempFile("nsc-font-", ".ttf");
				tmp.deleteOnExit();
				try (InputStream in = ManualRenderUtil.class.getResourceAsStream(path);
				     FileOutputStream fos = new FileOutputStream(tmp)) {
					if (in == null) {
						throw new BusinessException("中文字体文件缺失:resources" + path + "（须为TrueType轮廓，CFF轮廓的otf不被openhtmltopdf支持）");
					}
					in.transferTo(fos);
				}
				return tmp;
			} catch (BusinessException e) {
				throw e;
			} catch (Exception e) {
				throw new BusinessException("字体文件加载失败:" + e.getMessage());
			}
		});
	}
}
