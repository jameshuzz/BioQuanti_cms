







package net.mingsoft.basic.util;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;


/**
 * @ClassName: SpringUtil
 * @Description: TODO(Spring工具类)
 * @author 铭软开发团队
 * @date 2020年7月2日
 *
 */
@Component
public class SpringUtil implements ApplicationContextAware {

	private static ApplicationContext applicationContext;

	private static ThreadLocal<HttpServletRequest> requestThreadLocal=new ThreadLocal<>();
	/**
	 * 获取当前请求对象
	 *
	 * @return
	 */
	public static HttpServletRequest getRequest() {
		try {
			HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
			return request;
		} catch (Exception e) {
			return requestThreadLocal.get();
		}
	}
	/**
	 * 设置当前请求对象
	 *
	 * @return
	 */
	public static void setRequest(HttpServletRequest request) {
		requestThreadLocal.set(request);
	}

	/**
	 * 通过spring的webapplicationcontext上下文对象读取bean对象
	 *
	 * @param sc
	 *            上下文servletConext对象
	 * @param beanName
	 *            要读取的bean的名称
	 * @return 返回获取到的对象。获取不到返回null
	 */
	@Deprecated
	public static Object getBean(ServletContext sc, String beanName) {
		return getApplicationContext().getBean(beanName);
	}

	/**
	 * 通过spring的webapplicationcontext上下文对象读取bean对象
	 *
	 * @param beanName
	 *            要读取的bean的名称
	 * @return 返回获取到的对象。获取不到返回null
	 */
	public static Object getBean(String beanName) {
		return getApplicationContext().getBean(beanName);
	}

	/**
	 * 通过spring的webapplicationcontext上下文对象读取bean对象
	 *
	 * @param cls
	 *            要读取的类名称
	 * @return 返回获取到的对象。获取不到返回null
	 */
	public static <T> T getBean(Class<T> cls) {
		return getApplicationContext().getBean(cls);
	}

	/**
	 * 读取配置文件中的配置
	 * @param key 配置文件中对应的key
	 * @return 对应key配置的值
	 */
	public static String getProperty(String key) {
		return getApplicationContext().getEnvironment().getProperty(key);
	}



	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		if (SpringUtil.applicationContext == null) {
			SpringUtil.applicationContext = applicationContext;
		}
	}
	//获取applicationContext
	public static ApplicationContext getApplicationContext() {
		return applicationContext;
	}
}
