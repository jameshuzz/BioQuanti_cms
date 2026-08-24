









package net.mingsoft.basic.exception;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.extra.servlet.JakartaServletUtil;
import cn.hutool.json.JSONUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import net.mingsoft.base.entity.ResultData;
import net.mingsoft.base.util.BundleUtil;
import net.mingsoft.basic.biz.ILogBiz;
import net.mingsoft.basic.constant.Const;
import net.mingsoft.basic.constant.e.OperatorTypeEnum;
import net.mingsoft.basic.entity.LogEntity;
import net.mingsoft.basic.filter.XssHttpServletRequestWrapper;
import net.mingsoft.basic.util.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.UnauthorizedException;
import org.apache.shiro.session.ExpiredSessionException;
import org.apache.tomcat.util.http.fileupload.impl.SizeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.support.DefaultHandlerExceptionResolver;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 全局异常处理类
 *
 * @author 铭软开发团队-Administrator
 * @date 2018年4月6日
 * 历史修订:添加 BadSqlGrammarException处理 不返回具体sql,防止暴露表结构-2022-05-05
 */
@ControllerAdvice
public class GlobalExceptionResolver extends DefaultHandlerExceptionResolver {

    protected final Logger LOG = LoggerFactory.getLogger(this.getClass());


    @Autowired
    private ILogBiz logBiz;

    @Value("${ms.log.exclude-field:managerPassword,oldManagerPassword,newManagerPassword,newConfirmManagerPassword}")
    private String excludeFields;

    /**
     * 全局异常
     *
     * @param request
     * @param response
     * @param e
     * @return
     */
    @ExceptionHandler(value = BusinessException.class)
    @Deprecated
    public ModelAndView handleBusinessException(HttpServletRequest request, HttpServletResponse response, BusinessException e) {
        LOG.debug("handleBusinessException");
        response.setStatus(e.getCode().value());
        return render(request, response, ResultData.build().code(e.getCode()).data(e.getData()).msg(e.getMsg()), e);

    }

    /**
     * 全局异常
     *
     * @param request
     * @param response
     * @param e
     * @return
     */
    @ExceptionHandler(value = net.mingsoft.base.exception.BusinessException.class)
    public ModelAndView handleBusinessException(HttpServletRequest request, HttpServletResponse response, net.mingsoft.base.exception.BusinessException e) {
        LOG.debug("handleBusinessException");
        response.setStatus(e.getCode().value());
        return render(request, response, ResultData.build().code(e.getCode()).data(e.getData()).msg(e.getMsg()), e);

    }


    /**
     * 全局异常
     *
     * @param request
     * @param response
     * @param e
     * @return
     */
    @ExceptionHandler(value = Exception.class)
    public ModelAndView handleException(HttpServletRequest request, HttpServletResponse response, Exception e) {
        LOG.debug("handleException");
        response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        return render(request, response, ResultData.build().code(HttpStatus.INTERNAL_SERVER_ERROR)
                , e);
    }

    /**
     * sql异常,不返回具体sql,防止暴露表结构
     *
     * @param request
     * @param response
     * @param e
     * @return
     */
    @ExceptionHandler(value = org.springframework.jdbc.BadSqlGrammarException.class)
    public ModelAndView handleSqlException(HttpServletRequest request, HttpServletResponse response, Exception e) {
        LOG.debug("handleSqlException");
        response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        return render(request, response, ResultData.build().code(HttpStatus.INTERNAL_SERVER_ERROR).msg("系统异常，请查看系统日志！"), e);
    }


    /**
     * 自定义valid校验异常
     *
     * @param request
     * @param response
     * @param e
     * @return
     */
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public ModelAndView handleMethodArgumentNotValidException(HttpServletRequest request, HttpServletResponse response, MethodArgumentNotValidException e) {
        LOG.debug("handleMethodArgumentNotValidException");
        response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        String defaultMessage = e.getBindingResult().getFieldError().getDefaultMessage();
        return render(request, response, ResultData.build().code(HttpStatus.INTERNAL_SERVER_ERROR).msg(defaultMessage), e);
    }


    /**
     * 全局异常 未找到类404
     *
     * @param request
     * @param response
     * @param e
     * @return
     */
    @ExceptionHandler(value = NoHandlerFoundException.class)
    public ModelAndView handleNoHandlerFoundException(HttpServletRequest request, HttpServletResponse response, NoHandlerFoundException e) {
        LOG.debug("handleNoHandlerFoundException");
        return render(request, response, ResultData.build().code(HttpStatus.NOT_FOUND).msg("page 404"), e);
    }

    /**
     * 全局异常 未找到资源404
     *
     * @param request
     * @param response
     * @param e
     * @return
     */
    @ExceptionHandler(value = NoResourceFoundException.class)
    public ModelAndView handleNoResourceFoundException(HttpServletRequest request, HttpServletResponse response, NoResourceFoundException e) {
        LOG.debug("handleNoResourceFoundException");
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        return render(request, response, ResultData.build().code(HttpStatus.NOT_FOUND).msg("资源不存在"), e);
    }

    /**
     * 请求参数异常
     *
     * @param request
     * @param response
     * @param e
     * @return
     */
    @ExceptionHandler(value = MissingServletRequestParameterException.class)
    public ModelAndView handleMissingServletRequestParameterException(HttpServletRequest request, HttpServletResponse response, MissingServletRequestParameterException e) {
        LOG.debug("handleMissingServletRequestParameterException");
        return render(request, response, ResultData.build().code(HttpStatus.BAD_REQUEST).msg("请求参数异常"), e);
     }

    /**
     * 上传文件异常捕获
     *
     * @param request
     * @param response
     * @param e
     * @return
     * @throws IOException
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ModelAndView uploadException(HttpServletRequest request, HttpServletResponse response, MaxUploadSizeExceededException e) throws IOException {
        LOG.debug("MaxUploadSizeExceededException");
        response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        // 获取最大允许上传文件大小
        long maxFileSize = ((SizeException) e.getCause().getCause()).getPermittedSize();
        return render(request, response, ResultData.build().code(HttpStatus.INTERNAL_SERVER_ERROR)
                .msg(BundleUtil.getString("net.mingsoft.basic.resources.resources","upload.max.size", CalculationUtil.convertSpaceUnit(maxFileSize / 1024))), e);
    }

    /**
     * 请求方法类型错误
     *
     * @param request
     * @param response
     * @param e
     * @return
     */
    @ExceptionHandler(value = HttpRequestMethodNotSupportedException.class)
    public ModelAndView handleHttpRequestMethodNotSupportedException(HttpServletRequest request, HttpServletResponse response, HttpRequestMethodNotSupportedException e) {
        LOG.debug("handleHttpRequestMethodNotSupportedException");

        return render(request, response, ResultData.build().code(HttpStatus.METHOD_NOT_ALLOWED).msg("请求方法类型错误"), e);
    }

    /**
     * 统一处理请求参数校验(实体对象传参)
     *
     * @param e BindException
     * @return ResultResponse
     */
    @ExceptionHandler(BindException.class)
    public ModelAndView handleValidExceptionHandler(HttpServletRequest request, HttpServletResponse response, BindException e) {
        LOG.debug("handleValidExceptionHandler");
        StringBuilder message = new StringBuilder();
        List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();
        for (FieldError error : fieldErrors) {
            message.append(error.getField()).append(error.getDefaultMessage()).append(",");
        }
        message = new StringBuilder(message.substring(0, message.length() - 1));

        return render(request, response, ResultData.build().code(HttpStatus.NOT_ACCEPTABLE).msg(message.toString()), e);
    }

    /**
     * 统一处理请求参数校验(普通传参)
     *
     * @param e ConstraintViolationException
     * @return ResultResponse
     */
//    @ExceptionHandler(value = ConstraintViolationException.class)
//    public ModelAndView handleConstraintViolationException(HttpServletRequest request, HttpServletResponse response, ConstraintViolationException e) {
//        LOG.debug("handleConstraintViolationException");
//        StringBuilder message = new StringBuilder();
//        Set<ConstraintViolation<?>> violations = e.getConstraintViolations();
//        for (ConstraintViolation<?> violation : violations) {
//            Path path = violation.getPropertyPath();
//            String[] pathArr = StringUtils.splitByWholeSeparatorPreserveAllTokens(path.toString(), ".");
//            message.append(pathArr[1]).append(violation.getMessage()).append(",");
//        }
//        message = new StringBuilder(message.substring(0, message.length() - 1));
//
//        return render(request, response, ResultData.build().code(HttpStatus.INTERNAL_SERVER_ERROR).msg(message.toString()), e);
//    }

    /**
     * shiro权限未授权异常
     *
     * @param request
     * @param response
     * @param e
     * @return
     */
    @ExceptionHandler(value = UnauthorizedException.class)
    public ModelAndView handleUnauthorizedException(HttpServletRequest request, HttpServletResponse response, UnauthorizedException e) {
        LOG.debug("handleUnauthorizedException");
        return render(request, response, ResultData.build().code(HttpStatus.UNAUTHORIZED).msg("无访问权限!"), e);
    }

    /**
     * shiro权限未授权异常
     *
     * @param request
     * @param response
     * @param e
     * @return
     */
    @ExceptionHandler(value = LockedAccountException.class)
    public ModelAndView handleLockedAccountException(HttpServletRequest request, HttpServletResponse response, LockedAccountException e) {
        LOG.debug("handleLockedAccountException");
        response.setStatus(HttpStatus.LOCKED.value());
        return render(request, response, ResultData.build().code(HttpStatus.LOCKED).msg(e.getMessage()), e);
    }

    /**
     * shiro权限账号或密码错误
     *
     * @param request
     * @param response
     * @param e
     * @return
     */
    @ExceptionHandler(value = IncorrectCredentialsException.class)
    public ModelAndView handleIncorrectCredentialsException(HttpServletRequest request, HttpServletResponse response, IncorrectCredentialsException e) {
        LOG.debug("IncorrectCredentialsException");
        response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        return render(request, response, ResultData.build().code(HttpStatus.INTERNAL_SERVER_ERROR).msg("管理员账号或密码错误"), e);
    }

     /**
     * shiro权限账号错误
     *
     * @param request
     * @param response
     * @param e
     * @return
     */
    @ExceptionHandler(value = UnknownAccountException.class)
    public ModelAndView handleUnknownAccountException(HttpServletRequest request, HttpServletResponse response, UnknownAccountException e) {
        LOG.debug("UnknownAccountException");
        response.setStatus(HttpStatus.BAD_REQUEST.value());
        return render(request, response, ResultData.build().code(HttpStatus.BAD_REQUEST).msg("当前站点下此管理员账号不存在!"), e);
    }

    /**
     * 登录异常
     *
     * @param request
     * @param response
     * @param e
     * @return
     */
    @ExceptionHandler(value = AuthenticationException.class)
    public ModelAndView handleAuthenticationException(HttpServletRequest request, HttpServletResponse response, AuthenticationException e) {
        LOG.debug("AuthenticationException");
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        return render(request, response, ResultData.build().code(HttpStatus.UNAUTHORIZED).msg("身份认证异常"), e);
    }

    /**
     * shiro权限错误
     *
     * @param request
     * @param response
     * @param e
     * @return
     */
    @ExceptionHandler(value = AuthorizationException.class)
    public ModelAndView handleAuthorizationException(HttpServletRequest request, HttpServletResponse response, AuthorizationException e) {
        LOG.debug("AuthorizationException");

        return render(request, response, ResultData.build().code(HttpStatus.UNAUTHORIZED).msg("授权异常，请重新登录"), e);
    }


    /**
     * shiro权限错误
     *
     * @param e
     * @return
     */
    @ExceptionHandler(value = CredentialsException.class)
    public ModelAndView handleCredentialsException(HttpServletRequest request, HttpServletResponse response, CredentialsException e) {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        return render(request, response, ResultData.build().code(HttpStatus.UNAUTHORIZED).msg("&#x51ED;&#x8BC1;&#x5F02;&#x5E38;"), e);

    }


    /**
     * session失效异常
     *
     * @param request
     * @param response
     * @param e
     * @return
     */
    @ExceptionHandler(value = ExpiredSessionException.class)
    public ModelAndView handleExpiredSessionException(HttpServletRequest request, HttpServletResponse response, ExpiredSessionException e) {
        LOG.debug("ExpiredSessionException", e);
        response.setStatus(HttpStatus.GATEWAY_TIMEOUT.value());
        return render(request, response, ResultData.build().code(HttpStatus.GATEWAY_TIMEOUT), e);
    }

    /**
     * SQL异常拦截,避免敏感SQL信息暴露
     *
     * @param request
     * @param response
     * @param e
     * @return
     */
    @ExceptionHandler(value = SQLException.class)
    public ModelAndView handleSQLException(HttpServletRequest request, HttpServletResponse response, SQLException e) {
        LOG.debug("SQLException", e);
        response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        return render(request, response, ResultData.build().code(HttpStatus.INTERNAL_SERVER_ERROR).msg("系统异常,请查看系统日志"), e);
    }

    @ExceptionHandler(value = HttpMediaTypeNotAcceptableException.class)
    public ModelAndView handleHttpMediaTypeNotAcceptableException(HttpServletRequest request, HttpServletResponse response, HttpMediaTypeNotAcceptableException e) {
        LOG.debug("HttpMediaTypeNotAcceptableException", e);

        response.setStatus(HttpStatus.NOT_ACCEPTABLE.value());
        return render(request, response, ResultData.build().code(HttpStatus.NOT_ACCEPTABLE).msg("非法请求操作！"), e);
    }

    /**
     * 返回异常信息处理
     *
     * @param request
     * @param response
     * @param e
     * @return
     */
    private ModelAndView render(HttpServletRequest request, HttpServletResponse response, ResultData resultData, Exception e){
        // 获取转发前的请求地址和get queryString请求参数; 场景 IpFilter转发请求抛异常，在全局异常获取需要到真实触发异常请求地址
        String originUrl = (String) request.getAttribute("jakarta.servlet.forward.request_uri");

        originUrl = StringUtils.isBlank(originUrl) ? request.getRequestURI() : originUrl;

        LOG.debug("url: {}, ip: {}",originUrl, BasicUtil.getIp());
        // 如果是资源丢失不需要异常信息输出
        if(e instanceof NoResourceFoundException) {
            LOG.warn(e.getMessage());
            if (BasicUtil.isAjaxRequest(request)) {
                try {
                    response.setContentType("application/json;charset=UTF-8");
                    response.setStatus(resultData.getCode());
                    PrintWriter writer = response.getWriter();
                    writer.write(JSONUtil.toJsonStr(resultData));
                    writer.flush();
                    writer.close();
                }  catch (IOException ex) {
                    ex.printStackTrace();
                }
                // 不需要记录异常，简单提示一下就行
                return null;
            } else {
                return new ModelAndView("/error/index", resultData);
            }

        } else {
            e.printStackTrace();
        }

        LOG.debug("headers:{}", JSONUtil.toJsonStr(JakartaServletUtil.getHeaderMap(request)));

        String contextPath = request.getServletContext().getContextPath();
        //项目路径
        request.setAttribute(Const.BASE,contextPath);
        //设置当前地址参数，方便页面获取
        String requestPath = request.getServletPath();


        // TODO: 2024/12/31  携带resultData.msg的异常视为与用户有交互的异常，返回设置msg；非预期异常 返回默认的系统提示
        if (StringUtils.isBlank(resultData.getMsg())){
            resultData.msg(BundleUtil.getString("net.mingsoft.basic.resources.resources","sys.err"));
        }

        // 获取原始request，获取参数 不能使用BasicUtil.assemblyRequestMap 避免被xss二次处理
        HttpServletRequest originRequest;
        Map<String, String> requestParams = new HashMap<>();
        while (request instanceof HttpServletRequestWrapper) {
            if (request instanceof XssHttpServletRequestWrapper){
                originRequest = XssHttpServletRequestWrapper.getOrgRequest(request);
                requestParams = JakartaServletUtil.getParamMap(originRequest);

                break;
            }
            request = (HttpServletRequest) ((HttpServletRequestWrapper) request).getRequest();
        }

        // 避免产生过多的 getWriter() has already been called for this response
        if (!response.isCommitted()) {
            if (BasicUtil.isAjaxRequest(request) || e instanceof net.mingsoft.base.exception.BusinessApiException) {
                try {

                    response.setContentType("application/json;charset=UTF-8");
                    response.setStatus(resultData.getCode());
                    PrintWriter writer = response.getWriter();
                    writer.write(JSONUtil.toJsonStr(resultData));
                    writer.flush();
                    writer.close();

                } catch (IOException ex) {
                    ex.printStackTrace();
                } finally {
                    saveExceptionLog(requestParams, requestPath, e);
                }


            } else {
                saveExceptionLog(requestParams, requestPath, e);
                response.setStatus(resultData.getCode());
                return new ModelAndView("/error/index", resultData);
            }
        }


        return null;
    }

    /**
     * 因为这里使用了biz数据库,所以会将一个线程变为多线程,导致异常捕获不到
     * @param requestParams 请求参数
     * @param requestPath 请求地址
     * @param e 异常
     */
    private void saveExceptionLog(Map<String, String> requestParams, String requestPath,  Exception e) {
        List<StackTraceElement> stackElements = new ArrayList<>();
        stackElements.addAll(Arrays.asList(e.getStackTrace()));
        LogEntity log = new LogEntity();
        log.setLogResult(e.getMessage()==null?e.toString():e.getMessage());
        stackElements = this.getAllStackTrace(log, stackElements, e.getCause());
        List<String> stackList = stackElements.stream().filter(s -> s.getClassName().contains("net.mingsoft")).map(StackTraceElement::getFileName).filter(fileName -> Objects.requireNonNull(fileName).contains(".java")).collect(Collectors.toList());
        List<String> className = stackElements.stream().filter(s -> s.getClassName().contains("net.mingsoft")).filter(fileName -> Objects.requireNonNull(fileName.getFileName()).contains(".java")).map(StackTraceElement::getClassName).collect(Collectors.toList());
        if (CollUtil.isNotEmpty(className) && className.size() > 1) {
            log.setLogMethod(className.get(0)); //出错的类
        }else {
            log.setLogMethod(e.getStackTrace()[0].getClassName());
        }
        if (CollUtil.isNotEmpty(stackList) && className.size() > 1) {
            log.setLogTitle(stackList.get(0)); //异常标题
        }else {
            log.setLogTitle(e.getStackTrace()[0].getFileName());
        }
        // 写入请求参数
        if (CollUtil.isNotEmpty(requestParams)) {
            // 通过配置获取需要排除的字段
            String[] excludeField = ConfigUtil.getString("监控日志配置", "excludeField", excludeFields).split(",");
            log.setLogParam(JSONUtil.toJsonPrettyStr(BasicUtil.filter(requestParams, excludeField)));
        }
        log.setLogUrl(requestPath); //请求地址
        // 写入请求方式
        log.setLogRequestMethod(SpringUtil.getRequest().getMethod());
        // 写入堆栈信息
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        // 把日志写入到缓冲区，方便下面读取写入
        e.printStackTrace(pw);
        log.setLogErrorMsg(sw.toString()); //详细异常信息
        log.setLogLocation(IpUtils.getRealAddressByIp(BasicUtil.getIp())); // ip地理位置
        log.setCreateDate(new Date());
        log.setLogBusinessType("error");
        log.setLogStatus("error");
        if(BasicUtil.getManager()!=null) {
            log.setLogUser(BasicUtil.getManager().getManagerName());
            log.setLogUserType(OperatorTypeEnum.MANAGE.toString());
        }
        log.setLogIp(BasicUtil.getIp());
        logBiz.save(log);
        // TODO: 2023/7/28  方便log4j发出错误日志信息

        String client = "PC";
        if(BasicUtil.isMobileDevice()) {
            client = "移动端";
        }
        if(BasicUtil.isWechatBrowser()) {
            client = "微信";
        }
        //排除授权登陆的异常信息
        if(!e.getMessage().equalsIgnoreCase("请重新登陆")) {
            HttpServletRequest request = SpringUtil.getRequest();
            String originUrl = "无";
            if (request != null){
                originUrl = (String) request.getAttribute("jakarta.servlet.forward.request_uri");
                originUrl = StringUtils.isBlank(originUrl) ? request.getRequestURI() : originUrl;
            }
            LOG.error("\n GlobalExceptionResolver 提示 \n 来源{} \n 请求地址 {} \n 访客IP {} \n 异常原因{} \n 异常代码详情请查看后台日志管理",client,originUrl,BasicUtil.getIp() + IpUtils.getRealAddressByIp(BasicUtil.getIp()), e.getMessage()==null?e.toString():e.getMessage());
        }

    }

    /**
     * 递归获取所有异常及原因堆栈,填充日志msg
     * @param log 日志实体
     * @param traceElementList 堆栈数组
     * @param t 异常原因
     * @return 堆栈数组
     */
    private List<StackTraceElement> getAllStackTrace(LogEntity log, List<StackTraceElement> traceElementList, Throwable t) {
        if (t != null) {
            // 设置msg
            if (StringUtils.isBlank(log.getLogResult())) {
                log.setLogResult(t.getMessage());
            }
            // 递归获取所有的CauseTrace
            traceElementList.addAll(0, Arrays.asList(t.getStackTrace()));
            this.getAllStackTrace(log, traceElementList, t.getCause());
        }
        return traceElementList;
    }

}
