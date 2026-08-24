








package net.mingsoft.basic.aop;

import org.apache.commons.lang3.ArrayUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestBody;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * 切面基础
 *
 * @author ms dev group
 * @version 版本号：100-000-000<br/>
 *          创建日期：2012-03-15<br/>
 *          历史修订：<br/>
 */
public abstract class BaseAop {
    /*
     * log4j日志记录
     */
    protected final Logger LOG = LoggerFactory.getLogger(this.getClass());

    /**
     * 获取切面中的class 类
     * @param jp 拦截对象
     * @param clazz class 类名
     * @return 类存在则返回类否则为null
     */
    protected final <T> T getType(JoinPoint jp, Class<T> clazz) {
        Object[] objs = jp.getArgs();
        for (Object obj : objs) {
            if (obj!= null && obj.getClass() == clazz) {
                return (T) obj;
            }
        }
        return null;
    }

    protected final <T> T getType(JoinPoint jp, Class<T> clazz, boolean hasParent) {
        Object[] objs = jp.getArgs();
        for (Object obj : objs) {
            if (obj!= null &&  clazz.isInstance(obj)) {
                return (T) obj;
            }
        }
        return null;
    }

    /**
     * 获取aop请求中所有请求参数
     * @param jp
     * @return
     */
    protected final Object getJsonParam(JoinPoint jp) {
        Signature signature = jp.getSignature();
        MethodSignature methodSignature = (MethodSignature) signature;
        Method method = methodSignature.getMethod();
		Annotation[][] parameterAnnotations = method.getParameterAnnotations();
		for (Annotation[] parameterAnnotation : parameterAnnotations) {
            int paramIndex = ArrayUtils.indexOf(parameterAnnotations, parameterAnnotation);
            for (Annotation annotation : parameterAnnotation) {
                if (annotation instanceof RequestBody) {
                    return jp.getArgs()[paramIndex];
                }
            }
        }
        return null;
    }

    /**
     * 是否存在注解，如果存在就获取
     */
    protected final <T extends Annotation> T getAnnotation(JoinPoint jp, Class<T> an) {
        Signature signature = jp.getSignature();
        MethodSignature methodSignature = (MethodSignature) signature;
        Method method = methodSignature.getMethod();

        if (method != null) {
            return method.getAnnotation(an);
        }
        return null;
    }
}
