








package net.mingsoft.base.dao;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.mingsoft.base.entity.BaseEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 *
 * @ClassName:  IBaseDao
 * @Description:TODO(基础dao)
 * @date:   2018年3月19日 下午3:34:58
 *
 * @param <E>
 */
public interface IBaseDao<E> extends BaseMapper<E> {

	/**
	 * 根据id集合实现批量的删除
	 * @param ids id集合
	 */
	void delete(@Param("ids") int[] ids);

	/**
	 * 根据id集合实现批量的删除
	 * @param ids id集合
	 */
	void delete(@Param("ids") String[] ids);

	/**
	 * 更新缓存
	 * 使用场景：当前这个类存在数据缓存，使用了mybitsPlus的更新、保存等方法没有刷新数据缓存，
	 * 调用该方法需要dao xml中实现一个更新方法
	 * xml示例：
	 * 	<update id="updateCache"  flushCache="true">
	 * 		UPDATE table set del=0 where del=-1
	 * 	</update>
	 */
	void updateCache();

	/**
	 * 查询
	 */
	List<E> query(BaseEntity entity);


	/**
	 * SQL创建表
	 * @param table 表名称
	 * @param fileds key:字段名称 list[0] 类型 list[1]长度 list[2]默认值 list[3]是否不填
	 * 推荐使用IBaseBiz.execute方法替换
	 */
	@Deprecated
	void createTable(@Param("table") String table, @Param("fileds") Map<Object, List> fileds);

	/**
	 * SQL修改表
	 * @param table 表名称
	 * @param fileds key:字段名称 list[0] 类型 list[1]长度 list[2]默认值 list[3]是否不填
	 * 推荐使用IBaseBiz.execute方法替换
	 */
	@Deprecated
	void alterTable(@Param("table") String table, @Param("fileds") Map fileds, @Param("type") String type);

	/**
	 * SQL删除表
	 * @param table 表名称
	 * 推荐使用IBaseBiz.execute方法替换
	 */
	@Deprecated
	void dropTable(@Param("table") String table);

	/**
	 * SQL添加记录，推荐使用IBaseBiz.insert方法替换
	 * @param table 表名称
	 * @param fields 编号
	 * 推荐使用IBaseBiz.execute方法替换
	 */
	@Deprecated
	void insertBySQL(@Param("table") String table, @Param("fields") Map fields);

	/**
	 * SQL动态SQL删除，推荐使用IBaseBiz.update方法替换
	 * @param table 表名称
	 * @param wheres 条件 都是key-value对应
	 * 推荐使用IBaseBiz.execute方法替换
	 */
	@Deprecated
	void deleteBySQL(@Param("table") String table, @Param("wheres") Map wheres);

	/**
	 * SQL动态SQL更新，推荐使用IBaseBiz.update方法替换
	 * @param table 表名称
	 * @param fields list集合每个map都是key-value对应
	 * @param wheres 条件 都是key-value对应
	 * 推荐使用IBaseBiz.execute方法替换
	 */
	@Deprecated
	void updateBySQL(@Param("table") String table, @Param("fields") Map fields, @Param("wheres") Map wheres);

	/**
	 * SQL导入执行数据
	 * @param sql sql语句
	 * 推荐使用IBaseBiz.execute方法替换
	 */
	@InterceptorIgnore(tenantLine = "true")
	@Deprecated
	List excuteSql(@Param("sql") String sql);

	/**
	 * 批量新增
	 * @param list 新增数据
	 *过期理由不适配oracle,请使用mybatis-plus的biz.saveBatch批量保存，注：mybatis-plus在dao层没有批量保存方法
	 */
	@Deprecated
	void saveBatch(@Param("list") List list);

	/**
	 * 保存，推荐使用mp.save方法保存
	 * @param entity 实体
	 * @return 返回保存后的id
	 */
	@Deprecated
	int saveEntity(BaseEntity entity);

	/**
	 * 根据id删除实体 推荐使用delete(int[] ids)
	 * @param id 要删除的主键id
	 */
	@Deprecated
	void deleteEntity(int id);

	/**
	 * 通过entity条件删除对应entity
	 * @param entity
	 */
	@Deprecated
	void deleteByEntity(BaseEntity entity);

	/**
	 * 更新实体
	 * @param entity 实体
	 */
	@Deprecated
	void updateEntity(BaseEntity entity);

	/**
	 * 多表查询使用 单表推荐使用mp
	 * 根据ID查询实体信息
	 * @param id 实体ID
	 * @return 返回base实体
	 */
	BaseEntity getEntity(Integer id);

	/**
	 * 多表查询使用 单表推荐使用mp
	 * 根据ID查询实体信息
	 * @param id 实体ID
	 * @return 返回base实体
	 */
	BaseEntity getEntity(String id);


	/**
	 * 根据entity查询实体信息
	 * @param entity 实体
	 * @return 返回base实体
	 */
	@Deprecated
	<E>E getByEntity(BaseEntity entity);

	/**
	 * 查询所有，推荐使用query方法查询
	 * @return 返回list数组
	 */
	@Deprecated
	List<E> queryAll();

	/**
	 * 分页查询,4.5.8版本之后推荐使用query方法查询
	 * @param pageNo 页码
	 * @param pageSize 显示条数
	 * @param orderBy 排序字段
	 * @param order order 排序方式,true:asc;fales:desc
	 * @return 返回list数组
	 */
	@Deprecated
	List<E> queryByPage(@Param("pageNo") int pageNo, @Param("pageSize") int pageSize,
						@Param("orderBy") String orderBy, @Param("order") boolean order);

	/**
	 * 查询数据表中记录集合总数
	 * @return 返回查询总条数
	 */
	@Deprecated
	int queryCount();

}
