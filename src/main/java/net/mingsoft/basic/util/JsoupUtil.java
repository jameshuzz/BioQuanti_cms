


package net.mingsoft.basic.util;

import cn.hutool.core.net.URLDecoder;
import cn.hutool.core.util.StrUtil;
import net.mingsoft.base.util.SqlInjectionUtil;
import net.mingsoft.base.exception.BusinessException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Safelist;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

/**
 * Xss过滤工具
 */
public class JsoupUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(JsoupUtil.class);

    /**
     * html白名单
     */
    private static final Safelist WHITE_LIST = Safelist.relaxed().preserveRelativeLinks(true);

    /**
     * 配置过滤化参数,不对代码进行格式化
     */
    private static final Document.OutputSettings OUTPUT_SETTINGS = new Document.OutputSettings().prettyPrint(false);

    /**
     * 配置Unicode过滤参数
     */
    private static final String[] UNICODE_STR = {"+/v8","+/v9","+/v+","+/v/"};

    /**
     * xss脚本正则
     */
    private final static Pattern[] scriptPatterns = {
            // TODO: 2023/1/4 增加xss验证规则
            Pattern.compile("<script>(.*?)</script>", Pattern.CASE_INSENSITIVE),
            Pattern.compile("src[\r\n]*=[\r\n]*\\\'(.*?)\\\'", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
            Pattern.compile("</script>", Pattern.CASE_INSENSITIVE),
            Pattern.compile("<script(.*?)>", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
            Pattern.compile("eval\\((.*?)\\)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
            Pattern.compile("expression\\((.*?)\\)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
            Pattern.compile("javascript:", Pattern.CASE_INSENSITIVE),
            Pattern.compile("vbscript:", Pattern.CASE_INSENSITIVE),
            Pattern.compile("onload(.*?)=", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
            Pattern.compile("onerror(.*?)=", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
            Pattern.compile("onclick(.*?)=", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
            Pattern.compile("::\\$DATA", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL)
    };


    static {
        // 富文本编辑时一些样式是使用style来进行实现的 比如红色字体 所以需要给所有标签添加style属性
        // 富文本有视频上传，但是Jsoup没有加标签
        WHITE_LIST.addAttributes(":all","style","id","class","height","width","src").addAttributes("video", "data-setup");
    }

    /**
     * 判断content是否存在xss攻击
     * @param content
     * @return
     */
    public static  boolean hasXSS(String content) {
        if (UrlEncoderUtils.hasUrlEncoded(content)) {
            String encode = URLDecoder.decode(content, StandardCharsets.UTF_8);
            if (StrUtil.containsAny(encode,UNICODE_STR)) {
                return true;
            }
        }
        String temp = content;
        // 校验xss脚本
        for (Pattern pattern : scriptPatterns) {
            temp = pattern.matcher(temp).replaceAll("");
        }

        if (!temp.equals(content)) {
            return true;
        }
        return false;
    }

    /**
     * 清理xss
     * @param content 原始内容
     * @return 清理后的内容
     */
    public static String clean(String content) {
        if(hasXSS(content)) {
            LOGGER.error("xss content:{}",content);
            throw  new BusinessException("当前请求存在xss攻击，有问题的内容：".concat(content));
        }
        return Jsoup.clean(content, "", WHITE_LIST, OUTPUT_SETTINGS);
    }

    /**
     * 清理xss、与sql注入
     * @param content 原始内容
     * @return 清理后的内容
     */
    public static String cleanOrSqlInjection(String content) {
        if(hasXSS(content)) {
            LOGGER.error("xss content:{}",content);
            throw  new BusinessException("当前请求存在xss攻击，有问题的内容：".concat(content));
        }
        SqlInjectionUtil.filterContent(content);
        return content;
    }
}
