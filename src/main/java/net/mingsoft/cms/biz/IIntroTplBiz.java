package net.mingsoft.cms.biz;

import net.mingsoft.base.biz.IBaseBiz;
import net.mingsoft.cms.entity.IntroTplEntity;

/**
 * 产品简介模板业务层
 * @version
 * 版本号：1.0.0<br/>
 * 创建日期：2026-09-18<br/>
 */
public interface IIntroTplBiz extends IBaseBiz<IntroTplEntity> {

	/**
	 * 保存/更新模板（服务端按行数据拼装规整HTML，校验栏目绑定唯一）
	 * @param entity id为空新增，非空更新（content由rows拼装覆盖）
	 * @param rows 行数据JSON [{"label":"货号","value":""},...]
	 */
	void saveTpl(IntroTplEntity entity, String rows);

	/**
	 * 按栏目取绑定模板（编辑页注入用）
	 * @param categoryId 栏目id（新增文章场景）
	 * @param linkId 文章id（编辑场景，优先按文章所属栏目解析；含父栏目链向上匹配）
	 * @return 命中的模板，未绑定返回null
	 */
	IntroTplEntity getByCategory(String categoryId, String linkId);
}
