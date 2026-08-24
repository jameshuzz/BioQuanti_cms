








package net.mingsoft.base.job;

import org.quartz.Job;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @ClassName:  BaseJob   
 * @Description:TODO(基础job类)   
 * @date:   2018年3月19日 下午3:44:09   
 *     
 */
public abstract class BaseJob  implements Job {

	/*
	 * log4j日志记录
	 */
	protected final Logger LOG = LoggerFactory.getLogger(this.getClass());
	
	
}
