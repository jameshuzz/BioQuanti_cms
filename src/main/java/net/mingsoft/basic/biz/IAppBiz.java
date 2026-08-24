








package net.mingsoft.basic.biz;

import net.mingsoft.base.biz.IBaseBiz;
import net.mingsoft.basic.entity.AppEntity;

import java.util.Map;

/**
 * 网站基本信息业务层接口
 * @author 史爱华
 * @version
 * 版本号：100-000-000<br/>
 * 创建日期：2012-03-15<br/>
 * 历史修订：<br/>
 */
public interface IAppBiz extends IBaseBiz<AppEntity> {

	/**
	 * 查找域名相同站点的个数
	 * @param websiteUrl 域名
	 * @return 返回站点个数
	 */
	int countByUrl(String websiteUrl);

	/**
	 * 返回app表中的第一条数据,根据id排序
	 * 可能存在的情况: 返回null,说明数据库没有站点
	 * @return 返回站点实体
	 */
	AppEntity getFirstApp();

	/**
	 * 解析global标签的sql获取app信息
	 * @param map
	 * @return
	 */
	Map get(Map<String, Object> map);
}
