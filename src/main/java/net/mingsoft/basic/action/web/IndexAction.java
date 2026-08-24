









package net.mingsoft.basic.action.web;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import net.mingsoft.base.action.BaseAction;
import net.mingsoft.basic.biz.IAppBiz;
import net.mingsoft.basic.entity.AppEntity;
import net.mingsoft.basic.util.BasicUtil;
import net.mingsoft.basic.util.SpringUtil;
import net.mingsoft.config.MSProperties;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;

/**
 * @author by 铭软开发团队
 * @Description TODO
 * @date 2020/1/10 8:39
 */
@Hidden
@Controller("indexAction")
public class IndexAction extends BaseAction {

    private static String  INDEX = "index.html", DEFAULT = "default.html";
    /**
     * 注入站点业务层
     */
    private IAppBiz appBiz;

    /**
     * 访问站点主页
     *
     * @param req
     *            HttpServletRequest对象
     * @param res
     *            HttpServletResponse 对象
     * @throws ServletException
     *             异常处理
     * @throws IOException
     *             异常处理
     */
    @Operation(summary =  "访问站点主页")
    @GetMapping("/msIndex")
    public String index( HttpServletRequest req, HttpServletResponse res) throws IOException {
        String htmlDir = MSProperties.diy.htmlDir;
        LOG.debug("basic index");
        // 获取用户所请求的域名地址

        appBiz = SpringUtil.getBean(IAppBiz.class);
        // 查询数据库获取域名对应Id
        String dir = "";
        AppEntity website = BasicUtil.getApp();
        if (website != null) {
            dir = StringUtils.isNotBlank(website.getAppDir())? website.getAppDir() : "";
        } else {
            return "";
        }

        String defaultHtmlPath = BasicUtil.getRealPath(htmlDir + File.separator + dir + File.separator + DEFAULT);
        LOG.debug("defaultHtmlPath {}",defaultHtmlPath);
        File file = new File(defaultHtmlPath);
        String url = htmlDir + "/" +dir ;
        String indexPosition = url + "/" + INDEX;
        if (file.exists()) {
            indexPosition = url + "/" + DEFAULT;
        }
        LOG.debug("indexPosition {}",indexPosition);

        return "forward:"+indexPosition;
    }
}
