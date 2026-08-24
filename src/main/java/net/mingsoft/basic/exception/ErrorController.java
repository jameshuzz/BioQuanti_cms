







package net.mingsoft.basic.exception;

import cn.hutool.core.bean.BeanUtil;
import io.swagger.v3.oas.annotations.Hidden;
import net.mingsoft.base.entity.ResultData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.autoconfigure.web.servlet.error.BasicErrorController;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.Map;

/**
 * @author by 铭软开发团队
 * @Description 全局异常 GlobalExceptionResolver 不能处理到到统一由此类处理
 * @date 2020/1/10 8:39
 */
@Hidden
@Controller
@RequestMapping(value = {"${server.error.path:${error.path:/error}}"}, method = {RequestMethod.GET,RequestMethod.POST})
public class ErrorController extends BasicErrorController {

    protected final Logger LOG = LoggerFactory.getLogger(this.getClass());

    public ErrorController(ErrorAttributes errorAttributes) {
        super(errorAttributes, new ErrorProperties());
    }

    @Override
    public ModelAndView errorHtml(HttpServletRequest request, HttpServletResponse response) {
        HttpStatus status = getStatus(request);
        Map<String, Object> model = Collections
                .unmodifiableMap(getErrorAttributes(request, getErrorAttributeOptions(request, MediaType.TEXT_HTML)));
        response.setStatus(status.value());
        ModelAndView modelAndView = resolveErrorView(request, response, status, model);
        LOG.debug("errorhtml log meseeage：{},url：{}",model.get("message"),model.get("path"));
        return (modelAndView != null) ? modelAndView : new ModelAndView("/error/index",
                ResultData.build().code(model.get("status").toString())
                        .msg(model.get("error").toString()));
    }

    @Override
    public ResponseEntity<Map<String, Object>> error(HttpServletRequest request) {
        HttpStatus status = getStatus(request);
        if (status == HttpStatus.NO_CONTENT) {
            return new ResponseEntity<>(status);
        }
        Map<String, Object> body = getErrorAttributes(request, getErrorAttributeOptions(request, MediaType.ALL));
        LOG.debug("error status {}, error {},url {}",status,body,request.getRequestURI());
        BeanUtil.copyProperties(ResultData.build().code(status).msg(body.get("error").toString()),body);
        return new ResponseEntity<>(body, status);
    }
}
