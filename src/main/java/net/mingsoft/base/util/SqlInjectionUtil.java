
















package net.mingsoft.base.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.text.UnicodeUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.servlet.JakartaServletUtil;
import jakarta.servlet.http.HttpServletRequest;
import net.mingsoft.base.exception.BusinessException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * SQL注入工具类
 *
 * @author Administrator
 * @version 创建日期：2021/4/7 8:43<br/>
 * 历史修订：<br/>
 */
public class SqlInjectionUtil {

    private static final Logger LOG = LoggerFactory.getLogger(SqlInjectionUtil.class);


    /**
     * SQL注入过滤正则表达式
     * 用于匹配常见的SQL注入关键字和特殊字符
     * 包括但不限于：exec、insert、alter、select、drop、grant、delete、update等SQL关键字
     */
    private static final String REG = "\\b(exec|insert|alter|select|drop|grant|delete|update|count|chr|mid|master|truncate|char|declare|updatexml|system_user|extractvalue|floor|exp|linestring|multpolygon|database|multlinestring|multipoint|polygon|GeometryCollection|name_const|current_user|if|exists|dumpfile|outfile|case\s+?when|sleep|benchmark|user|version)\\b";

    /**
     * SQL注入过滤特殊符号正则表达式
     * 用于匹配常见的SQL注入特殊符号
     * 包括但不限于：分号、括号、注释符号、比较符号、@符号等
     */
    private static final String SQL_SYMBOL_REG = ";|\\(|\\)|/\\*[\\s\\S]*?\\*/|--|#|@|([<>])|([*;+'%])\\n";

    /**
     * SQL注释符号正则表达式
     * 用于匹配MySQL特有的注释语法 /*! ... */

    private static final String SQL_COMMENT_REG = "/\\*![\\s\\S]*?\\*/";

    /**
     * 表字段名正则
     * _ 错误
     * _a 正确
     * a 正确
     * __ 错误
     * a_b 正确
     * a_1 正确
     * 1_a 错误
     * _1 错误
     * _a_3 正确
     * _a__b 错误
     */
    private static final String tableColumnNameReg = "^(?!.*__)(?=.*[a-zA-Z])[a-zA-Z_][a-zA-Z0-9_]*$";

    /**
     * SQL注入关键字和特殊字符匹配 忽略大小写
     */
    private static Pattern sqlPattern = Pattern.compile(REG, Pattern.CASE_INSENSITIVE);

    /**
     * SQL注入特殊符号匹配
     */
    private static Pattern sqlSymbolPattern = Pattern.compile(SQL_SYMBOL_REG, Pattern.CASE_INSENSITIVE);

    /**
     * MYSQL注入注释符号匹配
     */
    private static final Pattern sqlCommentPattern = Pattern.compile(SQL_COMMENT_REG, Pattern.CASE_INSENSITIVE);

    /**
     * 表字段名正则
     * 格式 只有字母、数值、_，字母开头 且 不存在多个连续的_，允许_结尾
     */
    private static final Pattern tableColumnNamePattern = Pattern.compile(tableColumnNameReg);
    /**
     * sql注入过滤处理，遇到注入关键字抛异常
     *
     * @param values
     * @return
     */
    public static void filterContent(String... values) {
        filterContent(values, new String[0]);
    }

    /**
     * sql注入过滤处理，遇到注入关键字抛异常
     *
     * @param values
     * @param ignoreWords 忽略的关键字
     * @return
     */
    public static void filterContent(String[] values,String... ignoreWords) {
        for (String value : values) {
            if (value == null || "".equals(value)) {
                continue;
            }
            if (!SqlInjectionUtil.isSqlValid(value, ignoreWords)) {
                HttpServletRequest request = null;
                String clientIP = null;
                String url = null;
                try {
                    request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
                    clientIP = JakartaServletUtil.getClientIP(request);
                    url = request.getRequestURL().toString();
                } catch (Exception e) {
                    LOG.debug("通过request获取ip,url异常");
                    e.printStackTrace();
                }
                LOG.warn("请注意，检测到可能存在SQL注入风险: {}  当前请求地址为：{}，ip为：{}", value,url,clientIP);
                throw new BusinessException("此操作存在安全风险，具体请查看日志");
            }
        }
    }


    /**
     * 过滤map的sql注入过滤处理，遇到注入关键字抛异常
     * @param fields
     */
    public static void filterContent(Map<String,String> fields) {
        Iterator iterator = fields.keySet().iterator();
        while (iterator.hasNext()) {
            String key = iterator.next().toString();
            String value = fields.get(key);
            LOG.debug("key:{} value:{}", key,value);
            SqlInjectionUtil.filterContent(key);
            SqlInjectionUtil.filterContent(value);
        }
    }


    /**
     * 验证是否是合法字符串
     * @param str 需要验证的内容
     * @param ignoreWords 忽略的sql关键字或符号
     *        使用场景 某个业务一定会产生固定的sql关键字，通过手动调用去校验是否合法；如 自定义模型导入，固定会有(、)、create或alter，在exclude-field中配置自定义模型的model_json字段，再手动调用方法去验证model_json中的sql是否合法
     * @return true 合法，false 不合法
     */
    public static boolean isSqlValid(String str,String... ignoreWords) {
        // 强制unicode解码，防止通过unicode编码的字符串进行SQL注入
        str = UnicodeUtil.toString(str);
        Matcher commentMatcher = sqlCommentPattern.matcher(str);
        if (commentMatcher.find()) {
            LOG.warn("检测到SQL注释符号【{}】，请确认！", commentMatcher.group());
            return false;
        }
        Matcher matcher = sqlPattern.matcher(str);
        String sqlKeyWord = null;
        while (matcher.find()) {
            String sqlRegGroup = "";
            if(StringUtils.isNotBlank(matcher.group())) {
                sqlRegGroup = matcher.group();
                if (!StrUtil.containsAnyIgnoreCase(sqlRegGroup, ignoreWords)){
                    sqlKeyWord = sqlRegGroup;
                    break;
                }
            }
        }
        if (sqlKeyWord == null) {
            return true;
        }
        Matcher symbolMatcher = sqlSymbolPattern.matcher(str);
        while (symbolMatcher.find()) {
            String symbol = symbolMatcher.group();
            if (StringUtils.isNotBlank(symbol)) {
                if (!StrUtil.containsAnyIgnoreCase(symbol, ignoreWords)) {
                    LOG.warn("检测到SQL关键字 【{}】 和SQL符号 【{}】 同时存在，请确认！", sqlKeyWord, symbol);
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * 检查表名或字段名是否符合标准
     * @param names 表名或字段名
     */
    public static void checkStandardTableColumnName(String... names) {
        for (String name : names) {
            if (!tableColumnNamePattern.matcher(name).find()) {
                LOG.warn("请注意，表名或字段名【{}】不符合标准，请确认！", name);
                throw new BusinessException("表名或字段名不合法");
            }
        }
    }

    /**
     * 初始化sql注入拦截配置，如果不调用此函数，则使用默认配置
     * @param configMap 配置参数
     */
    public static void init(Map configMap) {
        if (CollUtil.isNotEmpty(configMap)) {
            LOG.info("sql injection configuration init");
            String sqlInjectionRegularExpression = MapUtil.getStr(configMap, "sqlInjectionKeyword", REG);
            String sqlInjectionSymbolRegularExpression = MapUtil.getStr(configMap, "sqlInjectionSymbol", SQL_SYMBOL_REG);
            sqlPattern = Pattern.compile(sqlInjectionRegularExpression, Pattern.CASE_INSENSITIVE);
            sqlSymbolPattern = Pattern.compile(sqlInjectionSymbolRegularExpression, Pattern.CASE_INSENSITIVE);
        }
    }

    /**
     * 获取当前请求地址，包括参数会才有?a=1&b=1方式组装成一条新的完整地址
     * @return 一条完整的get请求地址
     */
    public static String getRequestUrl() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        StringBuffer requestURL = request.getRequestURL();
        // 循环调用
//        Map<String, String[]> map = request.getParameterMap();
        return requestURL.toString();
    }
}
