package net.mingsoft.cms.action.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import net.mingsoft.base.entity.ResultData;
import net.mingsoft.basic.util.BasicUtil;
import net.mingsoft.basic.util.IpUtils;
import net.mingsoft.cms.biz.IMessageBiz;
import net.mingsoft.cms.entity.MessageEntity;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Date;

/**
 * 前台客户留言提交接口（开放，无需登录）
 * 创建日期：2026-08-24<br/>
 * 历史修订：<br/>
 */
@Tag(name = "前台-客户留言接口")
@Controller("WebcmsMessageAction")
@RequestMapping("/cms/message")
public class MessageAction extends net.mingsoft.cms.action.BaseAction {

    @Autowired
    private IMessageBiz messageBiz;

    /**
     * 提交留言，自动采集IP、IP归属地、来源页面、浏览器标识
     */
    @Operation(summary = "提交客户留言")
    @Parameter(name = "name", description = "姓名", required = true, in = ParameterIn.QUERY)
    @Parameter(name = "content", description = "留言内容", required = true, in = ParameterIn.QUERY)
    @PostMapping("/post")
    @ResponseBody
    public ResultData post(@RequestBody MessageEntity message, HttpServletRequest request) {
        // 必填校验
        if (message == null || StringUtils.isBlank(message.getName())) {
            return ResultData.build().error("请填写姓名");
        }
        if (StringUtils.isBlank(message.getContent())) {
            return ResultData.build().error("请填写留言内容");
        }
        // 至少一种联系方式
        if (StringUtils.isBlank(message.getEmail()) && StringUtils.isBlank(message.getPhone())
                && StringUtils.isBlank(message.getWechat()) && StringUtils.isBlank(message.getWhatsapp())
                && StringUtils.isBlank(message.getTelegram())) {
            return ResultData.build().error("请至少填写一种联系方式");
        }
        // 自动采集信息
        try {
            String ip = BasicUtil.getIp();
            message.setIp(ip);
            message.setIpRegion(IpUtils.getRealAddressByIp(ip));
        } catch (Exception e) {
            // 采集失败不影响提交
        }
        try {
            message.setReferer(StringUtils.substring(request.getHeader("Referer"), 0, 500));
        } catch (Exception ignored) {
        }
        try {
            message.setUserAgent(StringUtils.substring(request.getHeader("User-Agent"), 0, 500));
        } catch (Exception ignored) {
        }
        message.setStatus(0);
        message.setCreateDate(new Date());
        messageBiz.save(message);
        return ResultData.build().success("提交成功，我们会尽快与您联系");
    }
}
