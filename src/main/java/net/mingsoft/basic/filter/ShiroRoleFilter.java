
package net.mingsoft.basic.filter;

import cn.hutool.json.JSONUtil;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.mingsoft.base.entity.ResultData;
import net.mingsoft.basic.util.BasicUtil;
import org.apache.shiro.web.filter.authz.RolesAuthorizationFilter;
import org.apache.shiro.web.util.WebUtils;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * 自定义角色校验器，用于不同类型角色登录后相互请求正确提示和跳转页面
 */
public class ShiroRoleFilter extends RolesAuthorizationFilter {

    /**
     * 构造角色权限类，unauthorizedUrl为拦截后跳转的地址
     * @param unauthorizedUrl 跳出地址
     */
    public ShiroRoleFilter(String unauthorizedUrl) {
        setUnauthorizedUrl(unauthorizedUrl);
    }

    @Override
    public boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) throws IOException {
        return super.isAccessAllowed(request, response, mappedValue);
    }

    @Override
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws IOException {
        HttpServletRequest req = (HttpServletRequest)request;
        HttpServletResponse resp = (HttpServletResponse) response;
        if (BasicUtil.isAjaxRequest(req)) {
            //前端Ajax请求，则不会重定向
            resp.setHeader("Access-Control-Allow-Origin", req.getHeader("Origin"));
            resp.setHeader("Access-Control-Allow-Credentials", "true");
            resp.setContentType("application/json; charset=utf-8");
            resp.setCharacterEncoding("UTF-8");
            resp.setStatus(HttpStatus.UNAUTHORIZED.value());
            PrintWriter out = resp.getWriter();
            ResultData data = ResultData.build().code(HttpStatus.UNAUTHORIZED).msg("未检测到登录信息，请重新登录");
            out.println(JSONUtil.toJsonStr(data));
            out.flush();
            out.close();
        } else {
            String unauthorizedUrl = getUnauthorizedUrl();
            WebUtils.issueRedirect(request, response, unauthorizedUrl);
        }
        return false;
    }
}
