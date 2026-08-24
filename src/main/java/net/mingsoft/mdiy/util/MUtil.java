



package net.mingsoft.mdiy.util;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.springframework.web.util.HtmlUtils;

/**
 * 标签处理工具类
 */
public class MUtil {

    /**
     * html转内容纯文本
     *
     * @param content 含有html 标签的内容
     * @return 纯文本内容
     */
    public String html2text(String content) {
        if (StrUtil.isBlank(content)) {
            return "";
        }
        return Jsoup.parse(content).body().select("*").first().text();

    }

      /**
     * 生成一个简单格式的UUID字符串
     *
     * @return 返回生成的UUID字符串
     */
    public String uuid() {
        return IdUtil.fastSimpleUUID();
    }

    /**
     * 将字符串中的特殊字符转换为 HTML 实体，用于防御 XSS 攻击。
     * <p>
     * 该方法主要用于在 FreeMarker 模板中对动态内容进行二次处理，
     * 确保内容在 HTML 标签属性（如 title, value）或文本节点中展示时，
     * 引号、尖括号等字符不会被浏览器解析为 HTML 标签或属性结束符，避免被构造js事件方法等
     * </p>
     *
     * <pre>
     * 示例：
     * 输入：Mr." onmouseover="alert(1)"
     * 输出：Mr.&amp;quot; onmouseover=&amp;quot;alert(1)&amp;quot;
     * </pre>
     *
     * @param htmlContent 待转义的原始字符串（可能包含 HTML 特殊字符）
     * @return 转义后的安全字符串。如果输入为 null，则返回 null。
     * @see org.springframework.web.util.HtmlUtils#htmlEscape(String)
     */
    public String escape(String htmlContent) {
        if (StringUtils.isBlank(htmlContent)) {
            return htmlContent;
        }
        return HtmlUtils.htmlEscape(htmlContent);
    }

    /**
     * 将 HTML 实体字符串还原为原始字符串。
     * <p>
     * 该方法与 {@link #escape(String)} 相反，用于将经过 HTML 转义的内容恢复原样。
     * 例如将 &amp;lt; 还原为 <，&amp;quot; 还原为 " 等。
     * </p>
     *
     * <pre>
     * 示例：
     * 输入：Mr.&amp;quot; onmouseover=&amp;quot;alert(1)&amp;quot;
     * 输出：Mr." onmouseover="alert(1)"
     * </pre>
     *
     * @param htmlContent 待反转义的 HTML 实体字符串
     * @return 反转义后的原始字符串。如果输入为 null 或空白，则返回原值。
     * @see org.springframework.web.util.HtmlUtils#htmlUnescape(String)
     */
    public String unescape(String htmlContent) {
        if (StringUtils.isBlank(htmlContent)) {
            return htmlContent;
        }
        return HtmlUtils.htmlUnescape(htmlContent);
    }

}
