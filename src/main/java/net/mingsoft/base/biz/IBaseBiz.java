





package net.mingsoft.base.biz;

import com.baomidou.mybatisplus.extension.service.IService;
import net.mingsoft.base.entity.BaseEntity;
import org.springframework.lang.Nullable;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * @ClassName: BaseAction
 * @Description:TODO(基础接口，应用层可继承该类)
 * @date: 2018年3月19日 下午3:28:27
 */

public interface IBaseBiz<T> extends IService<T> {

    /**
     * 执行增、删、改sql语句
     *
     * @param prepareSql 预处理查询sql语句
     * @param params     参数，必须按预处理sql中的占位符顺序排列
     * @return 返回sql语句执行影响结果条数
     */
    int update(@Nullable String prepareSql,  @Nullable Object... params);


    /**
     * 执行查询结果集sql语句
     * @param sqlQueryWrapper 查询包装，处理参数、条件、排序
     * @return
     */
    List<Map<String, Object>> queryForList(@Nullable SqlQueryWrapper sqlQueryWrapper);

    /**
     * 执行查询结果集sql语句
     * @param sqlQueryWrapper 查询包装，处理参数、条件、排序
     * @return
     */
    SqlQueryWrapper.EUListBean queryForListPage(@Nullable SqlQueryWrapper sqlQueryWrapper);

    /**
     * 执行查询结果集sql语句
     *
     * @param prepareSql 预处理查询sql语句
     * @param arg        参数，必须按预处理sql中的占位符顺序排列
     * @return 返回sql语句执行结果
     */
    List<Map<String, Object>> queryForList(@Nullable String prepareSql, @Nullable Object... arg);

    /**
     * 执行查询结果集sql语句
     * @param prepareSql 预编译查询sql， eg: select * from cms_content where id = :id
     * @param params     预编译参数，key必须包括 预处理sql中所有:param的占位变量
     * @return 返回sql语句执行结果
     */
    List<Map<String, Object>> queryForListByNamedJdbc(@Nullable String prepareSql, @Nullable Map<String,Object> params);

    /**
     * 新增一条数据
     * @param tableName 表名称
     * @param fields 字段列和字段值集合map(非空)
     *               key：字段列名称
     *               value：字段列值
     * @return 返回sql语句执行影响结果条数
     */
    int insert(String tableName,Map fields);

    /**
     * 更新数据
     * @param tableName 表名称
     * @param fields 字段列和字段值集合map(非空)
     *               key：字段列名称
     *               value：字段列值
     * @param whereFields 条件字段列和字段值集合map，
     * @return 返回sql语句执行影响结果条数
     */
    int update(String tableName,Map fields,Map whereFields);

    /**
     * 删除数据
     * @param tableName 表名称
     * @param whereFields 条件字段列和字段值集合map，
     *                    key：字段列名称
     *                    value：字段列值
     * @return 返回sql语句执行影响结果条数
     */
    int delete(String tableName,Map whereFields);




    /**
     * 执行sql语句
     *
     * @param sql 完整sql，推荐是ddl语句 dml使用预处理方式避免注入问题
     */
    void execute(String sql);

    /**
     * 异步执行ddl sql语句；不干预主业务事务<br>
     * 如mysql、oracle 在执行ddl时会隐式提交事务，为了避免主业务事务被提交，通过@Async异步线程去执行ddl<br>
     * @param sql 完整ddl sql
     * @return future 对象，编程式做成功或异常的逻辑
     */
    CompletableFuture<Void> executeAsync(String sql);

    /**
     * 根据id集合实现批量的删除
     * 扩展雪花编号的id
     *
     * @param ids id集合
     */
    void delete(String[] ids);


    /**
     * 根据id集合实现批量的删除
     *
     * @param ids id集合
     */
    void delete(int[] ids);


    /**
     * 更新缓存
     * 使用场景：当前这个类存在数据缓存，使用了mybitsPlus的更新、保存等方法没有刷新数据缓存，
     * 调用该方法需要dao xml中实现一个更新方法
     * xml示例：
     * <update id="updateCache"  flushCache="true">
     * UPDATE table set del=0 where del=-1
     * </update>
     */
    void updateCache();

    /**
     * 查询，如果使用数据权限控制，推荐使用当前方法
     */
    List<T> query(BaseEntity entity);

    /**
     * 推荐使用：<br>
     * 创表等语句等推荐使用：IBaseBiz.execute(); <br>
     * 查询等语句等推荐使用：IBaseBiz.queryForList();<br>
     * 增删改业务都推荐使用：IBaseBiz.update();<br>
     * 导入执行数据
     *
     * @param sql sql语句
     */
    @Deprecated
    Object excuteSql(String sql);

    /**
     * 推荐使用MP的saveBatch();
     * 批量新增
     *
     * @param list 新增数据
     */
    @Deprecated
    void saveBatch(List list);

    /**
     * 推荐使用MP的save();<br>
     * 保存
     *
     * @param entity 实体
     * @return 返回保存后的id
     */
    @Deprecated
    int saveEntity(BaseEntity entity);

    /**
     * 推荐使用MP的remove();
     * 根据id删除实体
     *
     * @param entity 要删除的主键id
     */
    @Deprecated
    void deleteEntity(BaseEntity entity);

    /**
     * 推荐使用MP的removeById();<br>
     * 根据id删除实体
     *
     * @param id 要删除的主键id
     */
    @Deprecated
    void deleteEntity(int id);

    /**
     * 推荐使用MP的update();
     * 更新实体
     *
     * @param entity
     */
    @Deprecated
    void updateEntity(BaseEntity entity);

    /**
     * 推荐使用IBaseBiz.query();<br>
     * 查询所有
     *
     * @return 返回list实体数组
     */
    @Deprecated
    List<T> queryAll();

    /**
     * 推荐使用MP的count();<br>
     * 查询数据表中记录集合总数</br>
     * 可配合分页使用</br>
     *
     * @return 返回集合总数
     */
    @Deprecated
    int queryCount();

    /**
     * 推荐使用MP的getOne();<br>
     * 根据实体参数查询实体信息
     *
     * @param entity 实体
     * @return 返回实体
     */
    @Deprecated
    <E> E getEntity(BaseEntity entity);

    /**
     * 多表查询使用;单表查询推荐使用MP的getById();<br>
     * 根据ID查询实体信息
     *
     * @param id 实体ID
     * @return 返回实体
     */
    <E> BaseEntity getEntity(int id);

    /**
     * 多表查询使用;单表查询推荐使用MP的getById();<br>
     * 根据ID查询实体信息
     *
     * @param id 实体ID
     * @return 返回实体
     */
    <E> BaseEntity getEntity(String id);


    /**
     * 推荐使用：IBaseBiz.execute();<br>
     * 创建表
     *
     * @param table  表名称
     * @param fileds key:字段名称 list[0] 类型 list[1]长度 list[2]默认值 list[3]是否不填
     */
    @Deprecated
    void createTable(String table, Map<Object, List> fileds);

    /**
     * 推荐使用：IBaseBiz.execute();<br>
     * 删除表
     *
     * @param table 表名称
     */
    @Deprecated
    void dropTable(String table);

    /**
     * 推荐使用：IBaseBiz.execute();<br>
     * SQL修改表
     *
     * @param table  表名称
     * @param fileds key:字段名称 list[0] 类型 list[1]长度 list[2]默认值 list[3]是否不填
     */
    @Deprecated
    void alterTable(String table, Map fileds, String type);


    /**
     * 推荐使用IBaseBiz.insert(String,Map)代替
     *
     * @param table  表名称
     * @param fields 字段列、值组织的map
     */
    @Deprecated
    void insertBySQL(String table, Map fields);

    /**
     * 推荐使用IBaseBiz.update();<br>
     * 动态SQL删除
     *
     * @param table  表名称
     * @param wheres 條件 都是key-value对应
     */
    @Deprecated
    void deleteBySQL(String table, Map wheres);

    /**
     * 推荐使用IbaseBiz.update(String,Map,Map)<br>
     * 动态SQL更新
     *
     * @param table  表名称
     * @param fields list集合每个map都是key-value对应
     * @param wheres 条件 都是key-value对应
     */
    @Deprecated
    void updateBySQL(String table, Map fields, Map wheres);


}
