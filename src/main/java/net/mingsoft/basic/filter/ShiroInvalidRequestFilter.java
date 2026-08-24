
package net.mingsoft.basic.filter;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.shiro.lang.util.StringUtils;
import org.apache.shiro.web.filter.InvalidRequestFilter;
import org.apache.shiro.web.util.WebUtils;
import org.springframework.util.AntPathMatcher;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * 在 Shiro {@link InvalidRequestFilter} 上增加「排除路径」：匹配路径下的 URI 片段允许非 ASCII（如中文），
 * 其余校验（分号、反斜杠、路径穿越、编码点与斜杠等）与官方过滤器一致。
 * <p>
 * 因官方类中校验方法均为 private，无法只覆盖一处；本类继承以获得全部 block* 配置能力，仅重复校验实现这一小段（34-56行是新增方法和变量，在93行增加isExcluded()判断方法）。
 */
public class ShiroInvalidRequestFilter extends InvalidRequestFilter {

    private static final List<String> SEMICOLON = Collections.unmodifiableList(Arrays.asList(";", "%3b", "%3B"));

    private static final List<String> BACKSLASH = Collections.unmodifiableList(Arrays.asList("\\", "%5c", "%5C"));

    private static final List<String> FORWARDSLASH = Collections.unmodifiableList(Arrays.asList("%2f", "%2F"));

    private static final List<String> PERIOD = Collections.unmodifiableList(Arrays.asList("%2e", "%2E"));

    private PathTraversalBlockMode pathTraversalBlockMode = PathTraversalBlockMode.NORMAL;

    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    // 排除路径列表，例如：/upload/**, /template/**
    private Set<String> excludedPaths = Set.of();

    /**
     * 设置不进行「ASCII」检查的路径（Ant 风格，如 {@code /upload/**}）
     */
    public void setExcludedPaths(Set<String> excludedPaths) {
        this.excludedPaths = excludedPaths != null ? excludedPaths : Set.of();
    }

    private boolean isExcluded(String uri) {
        if (excludedPaths.isEmpty() || !StringUtils.hasText(uri)) {
            return false;
        }
        for (String pattern : excludedPaths) {
            if (PATH_MATCHER.match(pattern, uri)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected boolean isAccessAllowed(ServletRequest req, ServletResponse response, Object mappedValue) throws Exception {
        HttpServletRequest request = WebUtils.toHttp(req);
        return isValid(request.getRequestURI())
                && isValid(request.getServletPath())
                && isValid(request.getPathInfo());
    }

    @SuppressWarnings("checkstyle:BooleanExpressionComplexity")
    private boolean isValid(String uri) {
        return !StringUtils.hasText(uri)
                || (!containsSemicolon(uri)
                && !containsBackslash(uri)
                && !containsNonAsciiCharacters(uri))
                && !containsTraversal(uri);
    }

    private boolean containsSemicolon(String uri) {
        if (isBlockSemicolon()) {
            return SEMICOLON.stream().anyMatch(uri::contains);
        }
        return false;
    }

    private boolean containsBackslash(String uri) {
        if (isBlockBackslash()) {
            return BACKSLASH.stream().anyMatch(uri::contains);
        }
        return false;
    }

    private boolean containsNonAsciiCharacters(String uri) {
        // 如果启用了非ASCII字符拦截功能，并且当前URI不在排除路径中，
        if (isBlockNonAscii() && !isExcluded(uri)) {
            return !containsOnlyPrintableAsciiCharacters(uri);
        }
        return false;
    }

    private static boolean containsOnlyPrintableAsciiCharacters(String uri) {
        int length = uri.length();
        for (int i = 0; i < length; i++) {
            char c = uri.charAt(i);
            if (c < '\u0020' || c > '\u007e') {
                return false;
            }
        }
        return true;
    }

    private boolean containsTraversal(String uri) {
        if (pathTraversalBlockMode == PathTraversalBlockMode.NORMAL) {
            return !(isNormalized(uri));
        }
        if (pathTraversalBlockMode == PathTraversalBlockMode.STRICT) {
            return !(isNormalized(uri)
                    && PERIOD.stream().noneMatch(uri::contains)
                    && FORWARDSLASH.stream().noneMatch(uri::contains));
        }
        return false;
    }

    private boolean isNormalized(String path) {
        if (path == null) {
            return true;
        }
        for (int i = path.length(); i > 0; ) {
            int slashIndex = path.lastIndexOf('/', i - 1);
            int gap = i - slashIndex;
            if (gap == 2 && path.charAt(slashIndex + 1) == '.') {
                return false;
            }
            if (gap == 3 && path.charAt(slashIndex + 1) == '.' && path.charAt(slashIndex + 2) == '.') {
                return false;
            }
            i = slashIndex;
        }
        return true;
    }
}
