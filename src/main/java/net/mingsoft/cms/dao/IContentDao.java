

package net.mingsoft.cms.dao;

import net.mingsoft.base.dao.IBaseDao;
import net.mingsoft.cms.bean.CategoryBean;
import net.mingsoft.cms.bean.ContentBean;
import net.mingsoft.cms.entity.ContentEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 文章持久层
 * 创建日期：2019-11-28 15:12:32<br/>
 * 历史修订：<br/>
 */
public interface IContentDao extends IBaseDao<ContentEntity> {



    /**
     * 查询文章编号集合
     * @contentBean
     * @return
     */
    public List<CategoryBean> queryIdsByCategoryIdForParser(ContentBean contentBean);

    /**
     * 按文章id集合查询静态化所需数据（栏目模板/栏目路径/模型编号等），用于指定文章的定向静态化
     * @param ids 文章id集合
     * @return CategoryBean集合
     */
    public List<CategoryBean> queryBeansByArticleIds(@Param("ids") List<String> ids);

    /**
     * 查询文章编号集合,不包括单篇
     * @contentBean
     * @return
     */
    public List<CategoryBean> queryIdsByCategoryIdForParserAndNotCover(ContentBean contentBean);

    /**
     * 查询文章,不包括单篇
     * @contentBean
     * @return
     */
    public List<ContentBean> queryContent(ContentBean contentBean);

    /**
     * 根据查询文章实体总数
     *
     * @param tableName
     *            :自定义生成的表名
     * @param diyModel 自定义模型 field 和 value的Map
     * @param map
     *            key:字段名 value:List 字段的各种判断值 list[0]:是否为自定义字段 list[1]:是否为整形
     *            list[2]:是否是等值查询 list[3]:字段的值
     * @param categoryIds 栏目id集合
     * @return 文章实体总数
     */
    int getSearchCount(@Param("tableName") String tableName, @Param("diyModel") Map<String,Object> diyModel, @Param("map") Map<String, Object> map,
                        @Param("categoryIds") String categoryIds);

    /**
     * 分类编号删除文章
     * @param ids
     */
    void deleteEntityByCategoryIds(@Param("ids") String[] ids);
}
