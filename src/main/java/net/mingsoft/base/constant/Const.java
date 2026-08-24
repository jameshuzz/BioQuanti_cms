










package net.mingsoft.base.constant;

import java.util.ResourceBundle;
import org.springframework.context.ApplicationContext;


/**
 * @ClassName:  BaseAction   
 * @Description:TODO(这里用一句话描述这个类的作用)   
 * @date:   2018年3月19日 下午3:28:27   
 *     
 */
public final class Const {

	/**
	 * action层对应的国际化资源文件
	 */
	public final static String RESOURCES = "net.mingsoft.base.resources.resources";

	
	/**
	 * 默认编码格式
	 */
	public final static String UTF8 = "utf-8";
	
	/**
	 * URL路径符
	 */
	public final static String SEPARATOR ="/";
	

	/**
	 * 统一定义error错误值，用户返回消息统一
	 */
	public final static String ERROR ="error";
	
	/**
	 * 统一定义error错误值，用户返回消息统一
	 */
	public final static String ERROR_500 ="/500/error.do";
}
