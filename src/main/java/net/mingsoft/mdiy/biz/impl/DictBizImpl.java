



package net.mingsoft.mdiy.biz.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import net.mingsoft.base.biz.impl.BaseBizImpl;
import net.mingsoft.base.dao.IBaseDao;
import net.mingsoft.mdiy.biz.IDictBiz;
import net.mingsoft.mdiy.dao.IDictDao;
import net.mingsoft.mdiy.entity.DictEntity;

import java.util.List;


/**
 * 字典表管理持久化层
 * @version
 * 版本号：1.0.0<br/>
 * 创建日期：2016-9-8 17:11:19<br/>
 * 历史修订：<br/>
 */
 @Service("dictBizImpl")
public class DictBizImpl extends BaseBizImpl<IDictDao, DictEntity> implements IDictBiz {


	@Autowired
	private IDictDao dictDao;


		@Override
	protected IBaseDao getDao() {
		
		return dictDao;
	}


	@Override
	public DictEntity getByTypeAndLabelAndValue(String dictType, String dictLabel , String dictValue) {
		LambdaQueryWrapper<DictEntity> wrapper = new LambdaQueryWrapper<>();
		wrapper.eq(DictEntity::getDictType,dictType)
				.eq(StringUtils.isNotBlank(dictLabel), DictEntity::getDictLabel, dictLabel)
				.eq(StringUtils.isNotBlank(dictValue), DictEntity::getDictValue, dictValue);
		return dictDao.selectOne(wrapper);
	}

    @Override
    public List<DictEntity> dictType(DictEntity dictEntity)
    {
        return dictDao.dictType(dictEntity);
    }

	@Override
	public List queryExcludeApp(DictEntity dictEntity) {
		return dictDao.queryExcludeApp(dictEntity);
	}
}
