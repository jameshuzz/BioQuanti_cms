



package net.mingsoft.mdiy.biz;


import net.mingsoft.base.biz.IBaseBiz;
import net.mingsoft.mdiy.entity.DictEntity;

import java.util.List;

/**
 * 字典表业务
 * @version
 * 版本号：1.0.0<br/>
 * 创建日期：2016-9-8 17:11:19<br/>
 * 历史修订：<br/>
 */
public interface IDictBiz extends IBaseBiz<DictEntity> {
	/**
	 *
	 * 根据字典类型和标签名获取实体
	 * @param dictType 类型
	 * @param dictLabel 标签名
	 * @return DictEntity 字典实体
	 */
	public DictEntity getByTypeAndLabelAndValue(String dictType, String dictLabel , String dictValue);

    /**
     * 获取所有字典类型
     * 根据子数据类型获取所有字典类型
     * @param dictEntity
     * @return
     */
    List<DictEntity> dictType(DictEntity dictEntity);

	/**
	 * 使用站群排除appId拼接问题
	 * @param dictEntity
	 * @return
	 */
	List<DictEntity> queryExcludeApp(DictEntity dictEntity);
}
