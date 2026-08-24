


package net.mingsoft.basic.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import jakarta.servlet.http.HttpServletRequest;
import net.mingsoft.basic.biz.ILogBiz;
import net.mingsoft.basic.constant.e.BusinessTypeEnum;
import net.mingsoft.basic.constant.e.OperatorTypeEnum;
import net.mingsoft.basic.entity.LogEntity;
import org.apache.commons.lang3.StringUtils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 快速记录日志的工具类
 */
public class LogUtil {

    /**
     * 快速记录日志
     * @param title 日志标题
     * @param msg   日志信息
     * @param businessType   业务类型
     * @param businessId   业务id
     */
    public static void log(String title,String msg,String businessType,String businessId){
        //获取调用的类
        String className=new Exception().getStackTrace()[1].getClassName();
        LogEntity log = new LogEntity();
        log.setLogMethod(className); //出错的类
        log.setLogUrl(""); //请求地址
        log.setLogErrorMsg(msg); //详细异常信息
        log.setLogResult("");
        log.setLogLocation(IpUtils.getRealAddressByIp(BasicUtil.getIp())); // ip地理位置
        log.setLogTitle(title); //异常标题
        log.setCreateDate(new Date());
        log.setLogBusinessType(businessType);
        log.setBusinessId(businessId);
        log.setLogIp(BasicUtil.getIp());
        log(log);
    }

    /**
     * 快速记录日志
     * @param log 日志对象
     */
    public static void log(LogEntity log){
        //日志业务层
        ILogBiz logBiz = SpringUtil.getBean(ILogBiz.class);
        logBiz.save(log);
    }

    /**
     * 保存异常日志<br>
     *
     * @param e 异常对象
     * @param title 标题，可以为空，为空则从堆栈信息找出标题
     * @param params 请求参数， 可以为空
     */
    public static void saveExceptionLog(Exception e, Map params, String title, String requestUrl){
        List<StackTraceElement> stackElements = new ArrayList<>();
        LogEntity log = new LogEntity();
        log.setLogResult(e.getMessage()==null?e.toString():e.getMessage());
        stackElements = getAllStackTrace(stackElements, e.getCause());
        List<String> stackList = stackElements.stream().filter(s -> s.getClassName().contains("net.mingsoft")).map(StackTraceElement::getFileName).filter(fileName -> Objects.requireNonNull(fileName).contains(".java")).collect(Collectors.toList());
        List<String> className = stackElements.stream().filter(s -> s.getClassName().contains("net.mingsoft")).filter(fileName -> Objects.requireNonNull(fileName.getFileName()).contains(".java")).map(StackTraceElement::getClassName).collect(Collectors.toList());
        if (CollUtil.isNotEmpty(className) && className.size() > 1) {
            log.setLogMethod(className.get(0)); //出错的类
        }else {
            log.setLogMethod(e.getStackTrace()[0].getClassName());
        }
        // 看外部是否传入异常标题，如果有则写入外部异常标题
        if (StrUtil.isBlank(title)) {
            if (CollUtil.isNotEmpty(stackList) && className.size() > 1) {
                log.setLogTitle(stackList.get(0)); //异常标题
            }else {
                log.setLogTitle(e.getStackTrace()[0].getFileName());
            }
        } else {
            log.setLogTitle(title);
        }
        // 写入参数
        if (CollUtil.isNotEmpty(params)) {
            // 通过配置获取需要排除的字段
            String[] excludeField = ConfigUtil.getString("监控日志配置", "excludeField", "managerPassword,oldManagerPassword,newManagerPassword,newConfirmManagerPassword").split(",");
            log.setLogParam(JSONUtil.toJsonPrettyStr(BasicUtil.filter(params, excludeField)));
        }
        if (StrUtil.isNotBlank(requestUrl)) {
            log.setLogUrl(requestUrl);
        }
        HttpServletRequest request = SpringUtil.getRequest();
        if (request != null) {
            if (StrUtil.isBlank(requestUrl)) {
                log.setLogUrl(request.getRequestURI()); //请求地址
            }
            // 写入请求方式
            log.setLogRequestMethod(request.getMethod());
            String ip = BasicUtil.getIp();
            if (StringUtils.isNotBlank(ip)) {
                log.setLogIp(BasicUtil.getIp());
                log.setLogLocation(IpUtils.getRealAddressByIp(log.getLogIp())); // ip地理位置
            }
            if (BasicUtil.getManager() != null) {
                log.setLogUser(BasicUtil.getManager().getManagerNickName());
                log.setLogUserType(OperatorTypeEnum.MANAGE.toString());
            }
        }
        // 写入堆栈信息
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        // 把日志写入到缓冲区，方便下面读取写入
        e.printStackTrace(pw);
        log.setLogErrorMsg(sw.toString()); //详细异常信息
        log.setCreateDate(new Date());
        log.setLogBusinessType("error");
        log.setLogStatus("error");
        log(log);
    }

    /**
     * 递归获取所有异常及原因堆栈,填充日志msg
     * @param traceElementList 堆栈数组
     * @param t 异常原因
     * @return 堆栈数组
     */
    private static List<StackTraceElement> getAllStackTrace(List<StackTraceElement> traceElementList, Throwable t) {
        if (t != null) {
            // 递归获取所有的CauseTrace
            traceElementList.addAll(0, Arrays.asList(t.getStackTrace()));
            getAllStackTrace(traceElementList, t.getCause());
        }
        return traceElementList;
    }
}
