












package net.mingsoft.base.biz.impl;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import net.mingsoft.base.biz.IAsyncSqlBiz;
import net.mingsoft.base.biz.IBaseBiz;
import net.mingsoft.base.biz.SqlQueryWrapper;
import net.mingsoft.base.dao.IBaseDao;
import net.mingsoft.base.entity.BaseEntity;
import net.mingsoft.base.exception.BusinessException;
import net.mingsoft.base.util.SqlInjectionUtil;
import net.mingsoft.base.util.SqlUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.lang.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * @ClassName:  BaseAction
 * @Description:TODO(这里用一句话描述这个类的作用)
 * @date:   2018年3月19日 下午3:28:27
 *
 */
public abstract class BaseBizImpl<M extends BaseMapper<T>,T> extends ServiceImpl<M,T> implements IBaseBiz<T> {


	protected final Logger LOG = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	@Autowired
	@Lazy
	private IAsyncSqlBiz asyncSqlBiz;

	@Override
	public int update(String prepareSql, @Nullable Object... params){
		LOG.debug("update sql:{}", SqlUtil.parseSql(prepareSql, params));
		int update = jdbcTemplate.update(prepareSql, params);
		LOG.debug("updates: {}", update);
		return update;
	}


	@Override
	public List<Map<String,Object>> queryForList(@Nullable SqlQueryWrapper sqlQueryWrapper) {
		return queryForList(sqlQueryWrapper.getQuerySql(), sqlQueryWrapper.getParams());
	}

	/**
	 * 执行查询结果集sql语句
	 * @param sqlQueryWrapper 查询包装，处理参数、条件、排序
	 * @return
	 */
	public SqlQueryWrapper.EUListBean queryForListPage(@Nullable SqlQueryWrapper sqlQueryWrapper) {
		List<Map<String, Object>> list = queryForList(sqlQueryWrapper.getQuerySql(), sqlQueryWrapper.getParams());
		return new SqlQueryWrapper.EUListBean(list, sqlQueryWrapper.getTotal());
	}



	@Override
	public List<Map<String,Object>> queryForList(String prepareSql, @Nullable Object... params) {
		// 所有queryForList()语句都会走这一个，所以只需要在这里做打印SQL语句
		LOG.debug("queryForList sql:{}", SqlUtil.parseSql(prepareSql, params));
		return jdbcTemplate.queryForList(prepareSql,  params);
	}

	@Override
	public List<Map<String, Object>> queryForListByNamedJdbc(String prepareSql, Map<String,Object> params) {
		LOG.debug("queryForListByNamedJdbc sql:\n{}", SqlUtil.parseSql(prepareSql, params));
		return namedParameterJdbcTemplate.queryForList(prepareSql, params);
	}

	@Override
	public int insert(String tableName,Map  fields){
		if(MapUtil.isEmpty(fields)){
			throw new BusinessException("insert的字段列不能为空");
		}
		// 1. 动态生成 SQL
		String fieldNames = String.join(", ", fields.keySet()); // 字段名
		String placeholders = String.join(", ", Collections.nCopies(fields.size(), "?")); // 占位符

		String sql = StrUtil.format("INSERT INTO {} ({}) VALUES ({})", tableName, fieldNames, placeholders);
		// 2. 提取参数值
		Object[] params = fields.values().toArray();
		LOG.debug("insert sql: {}", SqlUtil.parseSql(sql, params));
		// 3. 执行 SQL
		return jdbcTemplate.update(sql, params);
	}

	@Override
	public int update(String tableName, Map  fields,Map  whereFields) {
		if(MapUtil.isEmpty(fields)){
			throw new BusinessException("update的字段列不能为空");
		}
		// 1. 动态生成 SQL
		String prepareSql = StrUtil.format("UPDATE {} ",tableName);
		List<Object> params = new ArrayList<>();
		String fieldNames = String.join(" = ? , ", fields.keySet());
		fieldNames = fieldNames + " = ?";
		prepareSql = StrUtil.format(prepareSql +"SET {} ",fieldNames);
		params.addAll(fields.values());
		if(MapUtil.isNotEmpty(whereFields)){
			String whereFieldNames = String.join(" = ? AND ", whereFields.keySet());
			whereFieldNames = whereFieldNames + " = ?";
			prepareSql = StrUtil.format(prepareSql +" WHERE {} ",whereFieldNames);
			params.addAll(whereFields.values());
		}

		// 2. 打印 执行sql
		LOG.debug("update sql: {}", SqlUtil.parseSql(prepareSql, params.toArray()));
		return jdbcTemplate.update(prepareSql, params.toArray());
	}

	@Override
	public int delete(String tableName, Map whereFields) {
		// 1. 动态生成 SQL
		String prepareSql = StrUtil.format("DELETE FROM {}  ",tableName);
		List<Object> params = new ArrayList<>();
		if(MapUtil.isNotEmpty(whereFields)){
			String whereFieldNames = String.join(" = ? AND ", whereFields.keySet());
			whereFieldNames = whereFieldNames + " = ?";
			prepareSql = StrUtil.format(prepareSql+" WHERE {} ",whereFieldNames);
			params.addAll(whereFields.values());
		}
		// 2. 打印 执行sql
		LOG.debug("delete sql: {}", SqlUtil.parseSql(prepareSql, params.toArray()));
		return jdbcTemplate.update(prepareSql, params.toArray());
	}

	@Override
	public void execute(String sql) {
		LOG.debug("execute sql:{}",sql);
		jdbcTemplate.execute(sql);
	}

	@Override
	public CompletableFuture<Void> executeAsync(String sql) {
		return asyncSqlBiz.executeDDL(sql);
	}

	@Override
	public void delete(int[] ids) {
		getDao().delete(ids);
	}
	@Override
	public void delete(String[] ids) {
		getDao().delete(ids);
	}

	@Override
	public void updateCache(){
		getDao().updateCache();
	}

	@Override
	public List<T> query(BaseEntity entity) {
		return getDao().query(entity);
	}

	/**
	 * 不需要重写此方法，自动会
	 *
	 * @return
	 */
	protected abstract IBaseDao<T> getDao();

	@Override
	public T getOne(Wrapper<T> queryWrapper, boolean throwEx) {
		return super.getOne(queryWrapper, throwEx);
	}

	@Override
	public Object excuteSql(String sql) {
		return getDao().excuteSql(sql);
	}

	@Override
	public void saveBatch(List list) {
		getDao().saveBatch(list);
	}

	@Override
	public int saveEntity(BaseEntity entity) {
		return getDao().saveEntity(entity);
	}

	@Override
	public void deleteEntity(BaseEntity entity) {
		getDao().deleteByEntity(entity);
	}

	@Override
	public void deleteEntity(int id) {
		getDao().deleteEntity(id);
	}

	@Override
	public void updateEntity(BaseEntity entity) {
		getDao().updateEntity(entity);
	}

	@Override
	public List<T> queryAll() {
		return getDao().queryAll();
	}

	@Override
	@Deprecated
	public int queryCount() {
		return getDao().queryCount();
	}

	@Override
	public T getEntity(BaseEntity entity) {
		return getDao().getByEntity(entity);
	}

	@Override
	public BaseEntity getEntity(int id) {
		return getDao().getEntity(id);
	}

	@Override
	public BaseEntity getEntity(String id) {
		return getDao().getEntity(id);
	}

	@Override
	public void createTable(String table, Map fileds) {
		SqlInjectionUtil.filterContent(table);
		getDao().createTable(table, fileds);
	}

	@Override
	public void dropTable(String table) {
		SqlInjectionUtil.filterContent(table);
		getDao().dropTable(table);
	}

	@Override
	public void alterTable(String table, Map fileds, String type) {
		SqlInjectionUtil.filterContent(table);
		getDao().alterTable(table, fileds, type);
	}

	@Override
	public void insertBySQL(String table, Map fields) {
		SqlInjectionUtil.filterContent(table);
		getDao().insertBySQL(table, fields);
	}

	@Override
	public void deleteBySQL(String table, Map wheres) {
		SqlInjectionUtil.filterContent(table);
		getDao().deleteBySQL(table, wheres);
	}

	@Override
	public void updateBySQL(String table, Map fields, Map wheres) {
		SqlInjectionUtil.filterContent(table);
		getDao().updateBySQL(table, fields, wheres);
	}



}
