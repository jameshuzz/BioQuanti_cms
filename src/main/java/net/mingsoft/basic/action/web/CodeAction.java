







package net.mingsoft.basic.action.web;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.CircleCaptcha;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import net.mingsoft.basic.constant.e.SessionConstEnum;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

/**
 * 图片验证码
 * @author by 铭软开发团队
 * @Description TODO
 * @date 2020/1/10 8:39
 */
@Tag(name = "前端-基础接口")
@Controller("codeAction")
@RequestMapping("code")
public class CodeAction {

    /**
     * 图片默认宽度
     */
    private int imgWidth = 100;

    /**
     * 图片默认高度
     */
    private int imgHeight = 50;

    /**
     * 验证码长度
     */
    @Value("${ms.rand-code.length:4}")
    private int length;

    /**
     * 验证码干扰数量
     */
    @Value("${ms.rand-code.circle:8}")
    private int circle;

    /**
     * 返回验证码图片
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
    @Operation(summary =  "返回验证码图片")
    @GetMapping(produces = MediaType.IMAGE_JPEG_VALUE)
    @ResponseBody
    public byte[] index(HttpServletRequest req, HttpServletResponse res, HttpSession session) throws IOException {
        CircleCaptcha captcha = CaptchaUtil.createCircleCaptcha(imgWidth, imgHeight, length, circle);
        // 将认证码存入SESSION
        session.setAttribute(SessionConstEnum.CODE_SESSION.toString(),  captcha.getCode());
        return captcha.getImageBytes();
    }
}
