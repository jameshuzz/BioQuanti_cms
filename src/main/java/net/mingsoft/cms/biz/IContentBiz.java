

package net.mingsoft.cms.biz;

import net.mingsoft.base.biz.IBaseBiz;
import net.mingsoft.basic.bean.EUListBean;
import net.mingsoft.cms.bean.CategoryBean;
import net.mingsoft.cms.bean.ContentBean;
import net.mingsoft.cms.entity.ContentEntity;
import net.mingsoft.mdiy.entity.ModelEntity;

import java.util.List;
import java.util.Map;


/**
 * 文章业务
 * 创建日期：2019-11-28 15:12:32<br/>
 * 历史修订：<br/>
 */
public interface IContentBiz extends IBaseBiz<ContentEntity> {


    /**
     * 根据文章属性查询
     * @param contentBean
     * @return
     */
    List<CategoryBean> queryIdsByCategoryIdForParser(ContentBean contentBean);
    /**
     * 查询文章,不包括单篇
     * @param contentBean
     * @return
     */
    List<ContentBean> queryContent(ContentBean contentBean);

    /**
     * 文章搜索结果总数，提供搜索使用
     * @param contentModel 文章模型，
     * @param diyModel 扩展模型字段 Map key:自定义模型字段  value:字段值
     * @param whereMap 条件
     * @param categoryIds 栏目编号集合 格式：1,2,3
     * @return 搜索总数
     */
    int getSearchCount(ModelEntity contentModel, Map<String,Object> diyModel, Map whereMap, String categoryIds);
    /**
     * 根据文章属性查询,不包括单篇
     * @param contentBean
     * @return
     */
    List<CategoryBean> queryIdsByCategoryIdForParserAndNotCover(ContentBean contentBean);

    /**
     * 根据解析标签arclist的sql获取list
     * @return 文章集合
     */
    EUListBean list(Map<String,Object> map);

    /**
     * 根据解析标签data的sql获取文章数据，如有自定义模型，则返回则加上自定义模型数据
     * @param map 解析参数map
     * @return 文章map
     */
    Map<String,Object> get(Map<String,Object> map);


}
