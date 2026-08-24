


package net.mingsoft.basic.filter;

import cn.hutool.json.JSONUtil;
import net.mingsoft.base.entity.ResultData;
import net.mingsoft.basic.util.BasicUtil;
import org.apache.shiro.web.filter.authc.FormAuthenticationFilter;
import org.springframework.http.HttpStatus;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.PrintWriter;

/**
 * 自定义登录过滤器 用于前后端分离异步请求时登录过期不能正确提示及跳转
 * 创建时间: 2023-6-14
 */
public class ShiroLoginFilter extends FormAuthenticationFilter {


    public ShiroLoginFilter() {
        super();
    }

    @Override
    //前端options请求放行
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) {
        HttpServletRequest request1 = (HttpServletRequest) request;
        //获取请求方式
        String method = request1.getMethod();
        //如果是options请求方式，则放行
        if("OPTIONS".equalsIgnoreCase(method)){
            return true;
        }

        return super.isAccessAllowed(request, response, mappedValue);
    }

    @Override
    //身份认证没有通过是的执行方法
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
        HttpServletRequest req = (HttpServletRequest)request;
        HttpServletResponse resp = (HttpServletResponse) response;
        if (this.isLoginRequest(request, response)) {
            if (this.isLoginSubmission(request, response)) {
                return this.executeLogin(request, response);
            } else {
                return true;
            }
        } else {
            if (BasicUtil.isAjaxRequest(req)) {
                //前端Ajax请求，则不会重定向
                resp.setHeader("Access-Control-Allow-Origin",  req.getHeader("Origin"));
                resp.setHeader("Access-Control-Allow-Credentials", "true");
                resp.setContentType("application/json; charset=utf-8");
                resp.setCharacterEncoding("UTF-8");
                resp.setStatus(HttpStatus.UNAUTHORIZED.value());
                PrintWriter out = resp.getWriter();
                ResultData data = ResultData.build().code(HttpStatus.UNAUTHORIZED).msg("未检测到登录信息，请重新登录");
                out.println(JSONUtil.toJsonStr(data));
                out.flush();
                out.close();
            }else {
                this.saveRequestAndRedirectToLogin(request, response);
            }
            return false;
        }
    }
}
