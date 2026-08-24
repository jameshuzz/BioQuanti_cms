



package net.mingsoft.basic.action.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import net.mingsoft.base.entity.ResultData;
import net.mingsoft.basic.action.BaseAction;
import net.mingsoft.basic.biz.IAppBiz;
import net.mingsoft.basic.util.BasicUtil;
import net.mingsoft.basic.util.ConfigUtil;
import net.mingsoft.config.MSProperties;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

/**
 * 网站基本信息控制层
 */
@Tag(name = "后端-基础接口")
@Controller("basicAppAction")
@RequestMapping("/basic/app")
public class AppAction extends BaseAction {

    /**
     * appBiz业务层的注入
     */
    @Autowired
    private IAppBiz appBiz;

    @Value("${ms.diy.html-dir:html}")
    private String htmlDir;


    /**
     * 获取站点信息
     *
     * @return 返回站点信息
     */
    @Operation(summary =  "获取站点信息")
    @GetMapping(value = "/get")
    @ResponseBody
    public ResultData get() {

        Map<String, Object> map = new HashMap<>();
        //全局参数设置
        map.put("html", htmlDir);
        //站点编号
        if (BasicUtil.getWebsiteApp() != null) {
            map.put("appDir", BasicUtil.getWebsiteApp().getAppDir());
            map.put("url", BasicUtil.getWebsiteApp().getAppHostUrl());
            map.put("appId", BasicUtil.getWebsiteApp().getAppId());
        } else {
            map.put("url", BasicUtil.getUrl());
            map.put("appDir", BasicUtil.getApp().getAppDir());
        }
        //对项目名预处理
        String contextPath = BasicUtil.getContextPath();
        if (StringUtils.isNotBlank(contextPath) && "/".equalsIgnoreCase(contextPath) ){
            contextPath = "";
        }
        map.putIfAbsent("contextPath", contextPath);
        map.put("template", ConfigUtil.getString("文件上传配置", "uploadTemplate", MSProperties.upload.template));
        return ResultData.build().success(appBiz.get(map));
    }

}
