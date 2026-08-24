







package net.mingsoft.basic.aop;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import net.mingsoft.base.entity.BaseEntity;
import net.mingsoft.base.entity.ResultData;
import net.mingsoft.basic.annotation.LogAnn;
import net.mingsoft.basic.biz.ILogBiz;
import net.mingsoft.basic.constant.e.BusinessTypeEnum;
import net.mingsoft.basic.entity.LogEntity;
import net.mingsoft.basic.entity.ManagerEntity;
import net.mingsoft.basic.util.BasicUtil;
import net.mingsoft.basic.util.ConfigUtil;
import net.mingsoft.basic.util.SpringUtil;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 * @author by 铭软开发团队
 * @Description TODO
 * @date 2019/11/20 12:04
 */
@Aspect
public abstract class BaseLogAop extends BaseAop{

    /**
     * 获取用户名
     * @return
     */
    public abstract String getUserName();

    /**
     * 是否切面
     * @return
     */
    public abstract boolean isCut(LogAnn log);

    /**
     * 切入点
     */
    @Pointcut("@annotation(net.mingsoft.basic.annotation.LogAnn)")
    public void logPointCut()
    { }

    /**
     * 处理完请求后执行
     *
     * @param joinPoint 切点
     */
    @AfterReturning(pointcut = "logPointCut()", returning = "result")
    public void doAfterReturning(JoinPoint joinPoint, Object result)
    {
        handleLog(joinPoint, null, result);
    }

    /**
     * 拦截异常操作
     *
     * @param joinPoint 切点
     * @param e 异常
     */
    @AfterThrowing(value = "logPointCut()", throwing = "e")
    public void doAfterThrowing(JoinPoint joinPoint, Exception e)
    {
        handleLog(joinPoint, e, null);
    }
    /**
     * 成功状态
     */
    private static final String SUCCESS="success";
    /**
     * 失败状态
     */
    private static final String ERROR="error";

    @Value("${ms.log.exclude-field:managerPassword,oldManagerPassword,newManagerPassword,newConfirmManagerPassword}")
    private String excludeFields;

    /**
     * 日志业务层
     */
    @Autowired
    private ILogBiz logBiz;

    private static final Logger LOG = LoggerFactory.getLogger(SystemLogAop.class);

    protected void handleLog(final JoinPoint joinPoint, final Exception e, Object result) {
        try{
            // 获得注解
            LogAnn controllerLog = getAnnotation(joinPoint, LogAnn.class);
            if (controllerLog == null){
                return;
            }
            if(!isCut(controllerLog)){
                return;
            }

            LogEntity logEntity = new LogEntity();
            //是否保存业务id
            if (controllerLog.saveId()){
                BaseEntity baseEntity = getType(joinPoint, BaseEntity.class,true);
                if (baseEntity != null){
                    logEntity.setBusinessId(baseEntity.getId());
                } else {// 批量操作的情况
                    ArrayList baseEntities = getType(joinPoint, ArrayList.class);
                    if (CollectionUtil.isNotEmpty(baseEntities)) {
                        List<String> ids = (List<String>) baseEntities.stream().map(entity -> {
                            if (entity instanceof BaseEntity) {
                                return ((BaseEntity) entity).getId();
                            }
                            return null;
                        }).collect(Collectors.toList());
                        ids.remove(null);
                        logEntity.setBusinessId(StrUtil.join(",",ids));
                    }
                }
            }
            logEntity.setLogUser(getUserName());
            logEntity.setLogStatus(SUCCESS);
            // 请求的地址
            String ip = BasicUtil.getIp();
            //设置IP
            logEntity.setLogIp(ip);

            // 通过配置获取需要排除的字段
            String[] excludeField = ConfigUtil.getString("监控日志配置", "excludeField", excludeFields).split(",");

            //设置返回参数
            // 判断返回参数是否是JSON类型，防止因为不是JSON对象，导致JSON解析失败
            if (ObjectUtil.isNotNull(result)) {
                if (JSONUtil.isTypeJSON(result.toString())) {
                    logEntity.setLogResult(JSONUtil.toJsonPrettyStr(BasicUtil.filter(result, excludeField)));
                } else {
                    logEntity.setLogResult(result.toString());
                }
            }
            //设置请求地址
            logEntity.setLogUrl(SpringUtil.getRequest().getRequestURI());

            if (e != null){
                logEntity.setLogStatus(ERROR);
                logEntity.setLogErrorMsg(StringUtils.substring(e.getMessage(), 0, 4000));
            }

            // 登录失败特殊处理
            if (BusinessTypeEnum.LOGIN.getLabel().equalsIgnoreCase(controllerLog.title())){

                ManagerEntity loginManager = this.getType(joinPoint, ManagerEntity.class);

                logEntity.setLogUser(loginManager.getManagerName());
                if (e == null) {
                    ResultData resultData = (ResultData) result;
                    if (!BooleanUtil.toBoolean(resultData.get("result").toString())){
                        logEntity.setLogStatus(ERROR);
                        logEntity.setLogErrorMsg(String.valueOf(resultData.get("msg")));
                    }
                }
            }

            // 设置方法名称
            String className = joinPoint.getTarget().getClass().getName();
            String methodName = joinPoint.getSignature().getName();
            logEntity.setLogMethod(className + "." + methodName + "()");
            // 设置请求方式
            logEntity.setLogRequestMethod(SpringUtil.getRequest().getMethod());

            // 设置action动作
            logEntity.setLogBusinessType(controllerLog.businessType().name().toLowerCase());
            // 设置标题
            logEntity.setLogTitle(controllerLog.title());
            // 设置操作人类别
            logEntity.setLogUserType(controllerLog.operatorType().name().toLowerCase());
            // 是否需要保存request，参数和值
            if (controllerLog.saveRequestData()) {
                this.setLogParam(joinPoint, logEntity, excludeField);
            }
            logEntity.setCreateDate(new Date());
            logBiz.saveData(logEntity);
        }
        catch (Exception exp){
            LOG.error("日志记录错误:{}", exp.getMessage());
            exp.printStackTrace();
        }
    }

    /**
     * 设置操作日志请求参数
     * @param joinPoint 切点
     * @param logEntity 日志实体
     */
    private void setLogParam(JoinPoint joinPoint, LogEntity logEntity, String... filterField) {
        // 获取参数的信息，传入到数据库中。
        boolean isJson = StringUtils.isNotBlank(SpringUtil.getRequest().getContentType()) && MediaType.valueOf(SpringUtil.getRequest().getContentType()).includes(MediaType.APPLICATION_JSON);
        //如果是json请求参数需要获取方法体上的参数
        if (isJson) {
            Object jsonParam = getJsonParam(joinPoint);
            if (ObjectUtil.isNotNull(jsonParam)) {
                Object json = BasicUtil.filter(jsonParam, filterField);
                logEntity.setLogParam(JSONUtil.toJsonPrettyStr(json));
            }
        } else {
            Map<String, Object> map = BasicUtil.assemblyRequestMap();
            logEntity.setLogParam(JSONUtil.toJsonPrettyStr(BasicUtil.filter(map, filterField)));
        }
    }

}
