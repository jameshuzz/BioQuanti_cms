package net.mingsoft.cms.action.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.mingsoft.base.exception.BusinessException;
import net.mingsoft.basic.util.BasicUtil;
import net.mingsoft.cms.biz.IContentBiz;
import net.mingsoft.cms.biz.IManualTemplateBiz;
import net.mingsoft.cms.entity.ContentEntity;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 前台说明书下载接口（匿名）
 * 静态附件下载：读取后台"一键生成"落盘的PDF附件（不再实时渲染）
 * 限流：同一产品+同一IP 60秒内仅允许下载1次
 * @version
 * 版本号：1.1.0<br/>
 * 创建日期：2026-08-30<br/>
 */
@Tag(name = "前端-说明书下载接口")
@RestController
@RequestMapping("/manual")
public class ManualDownloadAction {

	/**
	 * 限流窗口：60秒
	 */
	private static final long RATE_LIMIT_MILLIS = 60_000L;

	/**
	 * 限流记录：key=productId|ip, value=上次放行时间戳（单机内存，重启清零可接受）
	 */
	private final ConcurrentHashMap<String, Long> rateLimitMap = new ConcurrentHashMap<>();

	@Autowired
	private IManualTemplateBiz manualTemplateBiz;

	@Autowired
	private IContentBiz contentBiz;

	@Operation(summary = "下载产品说明书静态附件PDF")
	@GetMapping("/{productId}.do")
	public void download(@PathVariable("productId") String productId, HttpServletRequest request, HttpServletResponse response) {
		// 1. 参数校验：仅允许数字id
		if (StringUtils.isBlank(productId) || !productId.matches("\\d{1,20}")) {
			sendError(response, HttpServletResponse.SC_NOT_FOUND, "产品不存在");
			return;
		}
		// 2. 文章合法性：存在且已发布
		ContentEntity content = contentBiz.getById(productId);
		if (content == null || "1".equals(content.getContentDisplay())) {
			sendError(response, HttpServletResponse.SC_NOT_FOUND, "产品不存在或未发布");
			return;
		}
		// 3. 限流：同产品+同IP 60秒1次（仅统计成功下载，失败不占用限流次数）
		String ip = BasicUtil.getIp();
		String key = productId + "|" + ip;
		long now = System.currentTimeMillis();
		Long last = rateLimitMap.get(key);
		if (last != null && now - last < RATE_LIMIT_MILLIS) {
			sendError(response, 429, "下载过于频繁，请1分钟后再试");
			return;
		}

		// 4. 读取静态附件PDF（后台一键生成时落盘，无附件则404引导）
		Map<String, Object> result;
		try {
			result = manualTemplateBiz.getManualAttachment(productId);
		} catch (BusinessException e) {
			sendError(response, HttpServletResponse.SC_NOT_FOUND, e.getMessage());
			return;
		}
		byte[] pdf = (byte[]) result.get("bytes");

		// 5. 附件读取成功后才记录限流（失败重试不受1分钟限制）
		if (rateLimitMap.size() > 10_000) {
			rateLimitMap.entrySet().removeIf(e -> now - e.getValue() > RATE_LIMIT_MILLIS);
		}
		rateLimitMap.put(key, now);
		String fileName = String.valueOf(result.get("fileName"));
		try {
			response.reset();
			response.setContentType("application/pdf");
			response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''"
					+ URLEncoder.encode(fileName, StandardCharsets.UTF_8));
			response.setContentLength(pdf.length);
			try (OutputStream os = response.getOutputStream()) {
				os.write(pdf);
				os.flush();
			}
		} catch (Exception e) {
			throw new BusinessException("说明书下载失败:" + e.getMessage());
		}
	}

	/**
	 * 错误响应（纯文本，给前台用户看的提示）
	 */
	private void sendError(HttpServletResponse response, int status, String message) {
		try {
			response.reset();
			response.setStatus(status);
			response.setContentType("text/plain;charset=UTF-8");
			response.getOutputStream().write(message.getBytes(StandardCharsets.UTF_8));
			response.getOutputStream().flush();
		} catch (Exception ignored) {
			// 响应流异常时静默，客户端拿不到错误页
		}
	}
}
