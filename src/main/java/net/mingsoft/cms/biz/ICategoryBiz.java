

package net.mingsoft.cms.biz;

import net.mingsoft.base.biz.IBaseBiz;
import net.mingsoft.cms.entity.CategoryEntity;

import java.util.List;
import java.util.Map;


/**
 * 分类业务
 * 创建日期：2019-11-28 15:12:32<br/>
 * 历史修订：<br/>
 */
public interface ICategoryBiz extends IBaseBiz<CategoryEntity> {

    /**
     * 查询当前分类下的所有子分类,包含自身
     * @param category 通过setId指定栏目id
     * @return
     */
    List<CategoryEntity> queryChildren(CategoryEntity category);

    void saveEntity(CategoryEntity entity);

    /**更新父级及子集
     * @param entity
     */
    void updateEntity(CategoryEntity entity);

    /**只更新自身
     * @param entity
     */
    void update(CategoryEntity entity);

    void delete(String categoryId);

    void copyCategory(CategoryEntity entity);

    /**
     * 强转栏目类型
     * @param categoryEntity 栏目实体
     * @param targetCategoryType 目标栏目类型
     */
    void changeCategoryType(CategoryEntity categoryEntity,String targetCategoryType);

    /**
     * 解析标签channel的sql获取list
     * @param map 查询条件
     * @return 栏目集合
     */
    List<Map<String,Object>> list(Map<String,Object> map);
}
