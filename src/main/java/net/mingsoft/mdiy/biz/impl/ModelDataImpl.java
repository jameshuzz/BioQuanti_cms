





package net.mingsoft.mdiy.biz.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.map.CaseInsensitiveMap;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import net.mingsoft.base.biz.SqlQueryWrapper;
import net.mingsoft.base.biz.impl.BaseBizImpl;
import net.mingsoft.base.dao.IBaseDao;
import net.mingsoft.base.exception.BusinessException;
import net.mingsoft.base.util.BundleUtil;
import net.mingsoft.base.util.SqlInjectionUtil;
import net.mingsoft.basic.util.BasicUtil;
import net.mingsoft.basic.util.StringUtil;
import net.mingsoft.mdiy.biz.IModelBiz;
import net.mingsoft.mdiy.biz.IModelDataBiz;
import net.mingsoft.mdiy.constant.Const;
import net.mingsoft.mdiy.dao.IModelDao;
import net.mingsoft.mdiy.entity.ModelEntity;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Clob;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 自定义表单接口实现类
 * @author 铭软
 * @version
 * 版本号：100-000-000<br/>
 * 创建日期：2012-03-15<br/>
 * 历史修订：2022-1-21 queryDiyFormData() orderby非法参数
 * 历史修订：2026-05-13支持 orderItems 多字段独立升降序，与 orderBy/order 单字段兼容
 */
@Service()
@Transactional
public class ModelDataImpl extends BaseBizImpl implements IModelDataBiz {

	@Autowired
	private IModelBiz modelBiz;
	/**
	 * 注入自定义表单持久化层
	 */
	@Autowired
	private IModelDao modelDao;



	/**
	 * 获取类别持久化层
	 * @return diyFormDao 返回类别持久话层
	 */
	@Override
	protected IBaseDao getDao() {

		return modelDao;
	}

	@Override
	public boolean saveOrUpdate(String linkId) {
		// linkId为空直接返回
		if (StringUtils.isBlank(linkId)) {
			return false;
		}
		// 从请求体中获取模型数据
		String modelData = BasicUtil.getString("modelData");
		if (StrUtil.isBlank(modelData)) {
			return false;
		}

		// 模型数据转为Map，并且去除应外部传入的null值，''不受影响
		Map params = JSONUtil.toBean(JSONUtil.parseObj(modelData, true), Map.class);

		if (CollUtil.isEmpty(params)) {
			return false;
		}
		String modelId = MapUtil.getStr(params, "modelId");
		if (StringUtils.isBlank(modelId)) {
			return false;
		}

		// 赋值正确的Id
		params.put("linkId", linkId);

		// 获取是否有主键id字段
		CaseInsensitiveMap caseInsensitiveMap = new CaseInsensitiveMap<>(params);
		if (caseInsensitiveMap.containsKey("id")) {
			return this.updateDiyFormData(modelId, params);
		} else {
			return this.saveDiyFormData(modelId, params);
		}
	}

	@Override
	public boolean saveDiyFormData(String modelId, Map<String,Object> params) {
		ModelEntity model = modelBiz.getById(modelId);

		return saveDiyFormData(model,params);
	}

	@Override
	public boolean saveDiyFormData(ModelEntity model, Map<String, Object> params) {
		if (ObjectUtil.isNotNull(model)) {
			this.spliceInsertSql(model, params);
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean updateDiyFormData(String modelId, Map<String,Object> params) {
		ModelEntity model = modelBiz.getById(modelId);
		return updateDiyFormData(model,params);
	}

	@Override
	public boolean updateDiyFormData(ModelEntity model, Map<String, Object> params) {
		if (ObjectUtil.isNotNull(model)) {
			this.spliceUpdateSql(model, params);
			return true;
		} else {
			return false;
		}
	}

	@Override
	public SqlQueryWrapper.EUListBean queryDiyFormData(String modelId, Map<String,Object> params) {
		if (StringUtils.isBlank(modelId)) {
			return null;
		}
		ModelEntity model = modelBiz.getById(modelId);
		if(StringUtils.isBlank(model.getModelTableName())){
			return null;
		}
		SqlInjectionUtil.filterContent(model.getModelTableName());

		Map fieldMap = model.getFieldMap();
		HashMap<String, Object> fields = new HashMap<>();
		//拼接字段
		for (String s : params.keySet()) {
			//判断是否存在此字段
			if (fieldMap.containsKey(s)) {
				fields.put(fieldMap.get(s).toString(), params.get(s));
			}
		}
		List<SqlQueryWrapper.SqlWhere> sqlWhereList = new ArrayList<>();
		if(params.get("sqlWhere")!=null){
			sqlWhereList = JSONUtil.toList(params.get("sqlWhere").toString(),SqlQueryWrapper.SqlWhere.class);
			// sqlWhere字段做规范检测和存在校验
			sqlWhereList = sqlWhereList.stream().filter(sqlWhere -> {
				SqlInjectionUtil.checkStandardTableColumnName(sqlWhere.getField());
				CharSequenceUtil.toCamelCase(sqlWhere.getField());
				return fieldMap.containsKey(getCamelCaseString(sqlWhere.getField(), false));
			}).toList();
		}
		String orderBy = null;
		if(params.get("orderBy") !=null){
			orderBy = params.get("orderBy").toString();
		}
		String order = null;
		if(params.get("order") !=null){
			order = params.get("order").toString();
		}

		// 拼接SQL
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT ");
		List formFields = MapUtil.get(params, "formFields", List.class);
		// 过滤并收集前端请求中指定的合法查询字段
		List<String> formFieldValues = new ArrayList<>();
		if (CollUtil.isNotEmpty(formFields)) {
			// 收集模型中所有有效的数据库字段名（转为小写），用于后续校验前端请求的查询字段
			List<String> fieldValues = new ArrayList<>();
			for (Object value : fieldMap.values()) {
				if (value != null) {
					fieldValues.add(value.toString().toLowerCase());
				}
			}

			// 补充五个系统基础字段，确保这些常用字段即使不在模型配置中也能正确匹配
			fieldValues.addAll(Arrays.asList("id", "create_by", "create_date", "update_by", "update_date"));
			for (Object formField : formFields) {
				// 检查字段对象非空，且其小写形式存在于允许的字段集合中
				if (ObjectUtil.isNotNull(formField) && fieldValues.contains(formField.toString().toLowerCase())) {
					// 保留原始字段名大小写，用于SQL拼接
					formFieldValues.add(formField.toString());
				}
			}
		}
		
		// 构建SELECT子句：如果没有指定有效字段，默认查询全部(*)；否则拼接指定的字段列表
		if (CollUtil.isEmpty(formFieldValues)) {
			sb.append("*");
		} else {
			sb.append(CollUtil.join(formFieldValues, ","));
		}
		sb.append(" FROM ");
		// 添加表名
		sb.append(model.getModelTableName());
		// 判断是否是条件查询
		List<Object> paramsList = new ArrayList<>();
		if (CollUtil.isNotEmpty(fields)) {
			sb.append(" WHERE ");
			for (String key : fields.keySet()) {
				// TODO: 2024/12/20 疑问，这里不一定写死，只是目前看只有等于，如果后期有问题可以改成预处理，目前只是有疑问
				sb.append(key).append(" = ").append("?").append(" AND ");
				paramsList.add(fields.get(key));
			}
			// 删除多余的and
			sb.delete(sb.lastIndexOf("AND"), sb.length());
		}

		SqlQueryWrapper select = new SqlQueryWrapper(sb.toString(), paramsList.toArray()).sqlWhere(sqlWhereList);

		// 处理排序：orderItems 优先（多字段、每列可不同方向）；否则沿用 orderBy + order
		if (!applyOrderItems(select, fieldMap, params.get("orderItems"))) {
			if (StringUtils.isNotEmpty(orderBy)) {
				SqlInjectionUtil.filterContent(orderBy);
				if ("desc".equalsIgnoreCase(order)) {
					select.orderByDesc(orderBy);
				} else {
					select.orderByAsc(orderBy);
				}
			}
		}

		// 按需分页处理 有pageSize视为分页 否则查询全部
		if (params.containsKey(BasicUtil.PAGE_SIZE)) {
			int pageNo = Integer.parseInt(String.valueOf(params.get(BasicUtil.PAGE_NO)));
			int pageSize = Integer.parseInt(String.valueOf(params.get(BasicUtil.PAGE_SIZE)));
			select.page((pageNo - 1) * pageSize,pageSize);
		}

		return modelBiz.queryForListPage(select);
	}

	@Override
	public Object getFormData(String modelId,String id) {
		if(ObjectUtil.isNull(modelId) || StringUtils.isBlank(id) ){
			return null;
		}
		ModelEntity model = modelBiz.getById(modelId);
		if (model == null) {
			return null;
		}
		if (StringUtils.isBlank(model.getModelTableName())) {
			return null;
		}
		// 过滤表名
		SqlInjectionUtil.filterContent(model.getModelTableName());
		// 组织预处理SQL
		String sql = StrUtil.format("SELECT * FROM {} WHERE ID = ?", model.getModelTableName());
		List<Map<String, Object>> list = modelBiz.queryForList(sql, id);
		if(CollUtil.isEmpty(list)){
			return null;
		}
		HashMap<String, Object> modelEntity = new HashMap<>();
		Map<String,Object> fieldMap = model.getFieldMap();
		//拼接字段
		for (String s : list.get(0).keySet()) {
			//判断是否存在此字段
			for (Map.Entry<String, Object> entry : fieldMap.entrySet()) {
				if(s.equalsIgnoreCase(entry.getValue().toString())){
					modelEntity.put(entry.getKey(), list.get(0).get(s));
				}
			}
		}
		modelEntity.put("id",id);
		return modelEntity;
	}

	@Override
	public void deleteDiyFormData(String modelId,List<String> ids) {
		if(StrUtil.isBlank(modelId) || CollUtil.isEmpty(ids)){
			return;
		}
		ModelEntity model = modelBiz.getById(modelId);
		if (model == null) {
			return;
		}
		// 过滤表名
		SqlInjectionUtil.filterContent(model.getModelTableName());
		// 组织预处理SQL
		String inPlaceHolder = String.join(",", Collections.nCopies(ids.size(), "?"));
		String preSql = "DELETE FROM {} WHERE ID IN ( {} )";
		preSql = StrUtil.format(preSql,model.getModelTableName(), inPlaceHolder);
		modelBiz.update(preSql, ids.toArray());
	}


	@Override
	public void spliceInsertSql(ModelEntity model, Map<String, Object> params) {
		// 检查SQL注入, 由于不确定外部过来是否检测过表名,这里需要再次检测一次
		SqlInjectionUtil.filterContent(model.getModelTableName());
		//设置区分大小写的Map,因为代码生成器存在版本问题，有些老数据是大小写没统一
		Map fieldMap =  new CaseInsensitiveMap<>(model.getFieldMap());
		// 获取field字段信息
		List<Map> fieldList = JSONUtil.toList(model.getModelField(), Map.class);
		// 存储参数
		List<Object> param = new ArrayList<>();
		// 构建SQL
		StringBuilder sql = new StringBuilder();
		sql.append("INSERT INTO ").append(model.getModelTableName()).append(" (");
		// 由于这个有值，所以在构建一个value集合
		StringBuilder values = new StringBuilder();
		// 把传递的参数key转换为小写
		params =  new CaseInsensitiveMap<>(params);
		Set<String> paramKeys = Collections.unmodifiableSet(new HashSet<>(params.keySet()));
		// 字段名称
		String fieldName = "";
		// 字段类型
		String fieldType = "";
		// java类型
		String javaType = "";
		// 通过处理后的字段值
		String value = "";
		// 是否必填
		boolean isRequired = false;
		for (Map map : fieldList) {
			fieldName = MapUtil.getStr(map, "model").toLowerCase();
			// 判断当前字段是否必填
			isRequired = MapUtil.getBool(map, "isRequired", false);
			if (isRequired && StrUtil.isBlank(MapUtil.getStr(params, fieldName))) {
				throw new BusinessException(BundleUtil.getBaseString("err.empty", MapUtil.getStr(map, "name")));
			}
			// 如果 fieldMap 中有对应的 paramKey 并且它匹配当前的字段名，则继续
			if (paramKeys.contains(fieldName)) {
				fieldType = MapUtil.getStr(map, "type", "String");
				// 时间会有一个多时间选择组件，他的java类型为String，而type类型为Date，导致导致会走时间处理
				javaType = MapUtil.getStr(map, "javaType", "String");
				// 根据类型处理不同的情况
				if (("date".equalsIgnoreCase(fieldType) || "time".equalsIgnoreCase(fieldType)) && !"string".equalsIgnoreCase(javaType)) {
					// 获取类型处理返回后的值
					value = new SqlQueryWrapper().handleTime(fieldType);
					sql.append(fieldMap.get(fieldName)).append(", ");
					values.append(value).append(", ");
				} else {
					sql.append(fieldMap.get(fieldName)).append(", ");
					values.append("?, ");
				}
				param.add(params.get(fieldName));
			}
		}
		// TODO: 2023/11/15 idType为0是雪花id
		if (model.getModelIdType()==0){
			Snowflake snowflake = IdUtil.getSnowflake();
			sql.append("ID, ");
			values.append("?, ");
			param.add(snowflake.nextId());
		}
		// 处理LINK_ID容错处理
		if (StrUtil.isNotBlank(MapUtil.getStr(params, "linkid"))){
			sql.append("LINK_ID, ");
			values.append("?, ");
			param.add(MapUtil.getStr(params, "linkid"));
		}
		if (BasicUtil.getManager()!=null){
			// 前台提交做容错
			sql.append("CREATE_BY, ");
			values.append("?, ");
			param.add(BasicUtil.getManager().getId());
		}
		sql.append("CREATE_DATE, ").append("UPDATE_DATE").append(") VALUES (");
		values.append("?, ").append("?");
		param.add(DateUtil.date());
		param.add(DateUtil.date());
		// 拼接参数值
		sql.append(values).append(")");
		modelBiz.update(sql.toString(), param.toArray());
	}

	@Override
	public void spliceUpdateSql(ModelEntity model, Map<String, Object> params) {
		// 检查SQL注入, 由于不确定外部过来是否检测过表名,这里需要再次检测一次
		SqlInjectionUtil.filterContent(model.getModelTableName());
		// 把传递的参数key转换为小写
		params =  new CaseInsensitiveMap<>(params);
		if (StringUtils.isBlank(MapUtil.getStr(params, "id"))) {
			throw new BusinessException(BundleUtil.getBaseString("err.empty", BundleUtil.getString(Const.RESOURCES,"id")));
		}
		Map fieldMap = model.getFieldMap();
		fieldMap = new CaseInsensitiveMap<>(fieldMap);
		// 组织参数
		List<Object> fields = new ArrayList<>();
		// 构建预处理SQL
		StringBuilder sql = new StringBuilder();
		sql.append("UPDATE ").append(model.getModelTableName()).append(" SET ");
		// 获取field字段信息
		List<Map> fieldList = JSONUtil.toList(model.getModelField(), Map.class);
		Set<String> paramKeys = Collections.unmodifiableSet(new HashSet<>(params.keySet()));
		// 字段名称
		String fieldName = "";
		// 字段类型
		String fieldType = "";
		// java类型
		String javaType = "";
		// 通过处理后的字段值
		String value = "";
		// 是否必填
		boolean isRequired = false;
		for (Map map : fieldList) {
			fieldName = MapUtil.getStr(map, "model").toLowerCase();
			// 判断当前字段是否必填
			isRequired = MapUtil.getBool(map, "isRequired", false);
			if (isRequired && StrUtil.isBlank(MapUtil.getStr(params, fieldName))) {
				throw new BusinessException(BundleUtil.getBaseString("err.empty", MapUtil.getStr(map, "name")));
			}
			// 如果 fieldMap 中有对应的 paramKey 并且它匹配当前的字段名，则继续
			if (paramKeys.contains(fieldName)) {
				fieldType = MapUtil.getStr(map, "type", "String");
				// 时间会有一个多时间选择组件，他的java类型为String，而type类型为Date，导致导致会走时间处理
				javaType = MapUtil.getStr(map, "javaType", "String");
				// 根据类型处理不同的情况
				if (("date".equalsIgnoreCase(fieldType) || "time".equalsIgnoreCase(fieldType)) && !"string".equalsIgnoreCase(javaType)) {
					// 获取类型处理返回后的值
					value = new SqlQueryWrapper().handleTime(fieldType);
					sql.append(fieldMap.get(fieldName)).append(" = ")
							.append(value).append(", ");
				} else {
					sql.append(fieldMap.get(fieldName)).append(" = ").append("?, ");
				}
				fields.add(params.get(fieldName));
			}
			// 特殊情况，在下拉框中，如果选项为空，则不会携带参数传入
			if ("select".equalsIgnoreCase(fieldType) && !paramKeys.contains(fieldName)) {
				sql.append(fieldMap.get(fieldName)).append(" = null, ");
			}
		}
		if (BasicUtil.getManager() != null) {
			sql.append("UPDATE_BY = ?,");
			fields.add(BasicUtil.getManager().getId());
		}
		sql.append("UPDATE_DATE = ? ");
		fields.add(DateUtil.date());
		sql.append(" WHERE ID = ?");
		fields.add(params.get("id").toString());
		modelBiz.update(sql.toString(), fields.toArray());
	}


	@Override
	public Map getModelDataByLinkId(ModelEntity model, String linkId) {
		if(ObjectUtil.isNull(model) || StringUtils.isBlank(linkId)){
			return null;
		}
		if(StringUtils.isBlank(model.getModelTableName())){
			return null;
		}
		SqlInjectionUtil.filterContent(model.getModelTableName());
		Map data=new HashMap();
		List<Map<String,Object>> listMap = modelBiz.queryForList(StrUtil.format("SELECT * FROM {} WHERE LINK_ID = ?",model.getModelTableName()),linkId);
		if(listMap.size()>0){
			for (Object o : listMap.get(0).keySet()) {
				Object _o = listMap.get(0).get(o);
				if (_o instanceof Clob) {
					_o = StringUtil.clobStr((Clob) _o);
				}
				data.put(getCamelCaseString(o.toString(),false), _o);
			}
		}
		return data;
	}

	/**
	 * 多字段排序：orderItems 为 JSON 数组，每项仅含 field（须为模型 fieldMap 的 key）、order（asc/desc，忽略大小写）。
	 * 至少成功应用一条时返回 true。
	 */
	private boolean applyOrderItems(SqlQueryWrapper select, Map fieldMap, Object orderItemsRaw) {
		if (orderItemsRaw == null || StringUtils.isBlank(orderItemsRaw.toString())) {
			return false;
		}
		List<Map> orderItems;
		try {
			orderItems = JSONUtil.toList(orderItemsRaw.toString(), Map.class);
		} catch (Exception e) {
			return false;
		}
		if (CollUtil.isEmpty(orderItems)) {
			return false;
		}
		boolean applied = false;
		for (Map item : orderItems) {
			String fieldKey = MapUtil.getStr(item, "field");
			if (StringUtils.isBlank(fieldKey) || !fieldMap.containsKey(getCamelCaseString(fieldKey,false))) {
				continue;
			}

			SqlInjectionUtil.filterContent(fieldKey);
			String ord = MapUtil.getStr(item, "order");
			if ("desc".equalsIgnoreCase(ord)) {
				select.orderByDesc(fieldKey);
			} else {
				select.orderByAsc(fieldKey);
			}
			applied = true;
		}
		return applied;
	}

	/**
	 * 驼峰命名法转换工具 如将USER_NAME => userName,分隔符可以为 '_' 或者 '-' 或者 '@' 或者 '$' 或者 '#' 或者 ' ' 或者 '/' 或者 '&'
	 * @param inputString 输入字符串
	 * @param firstCharacterUppercase 首字母是否大写 true大写，false小写
	 * @return
	 */
	private  String getCamelCaseString(String inputString, boolean firstCharacterUppercase) {
		StringBuilder sb = new StringBuilder();
		boolean nextUpperCase = false;
		for (int i = 0; i < inputString.length(); i++) {
			char c = inputString.charAt(i);

			switch (c) {
				case '_':
				case '-':
				case '@':
				case '$':
				case '#':
				case ' ':
				case '/':
				case '&':
					if (sb.length() > 0) {
						nextUpperCase = true;
					}
					break;

				default:
					if (nextUpperCase) {
						sb.append(Character.toUpperCase(c));
						nextUpperCase = false;
					} else {
						sb.append(Character.toLowerCase(c));
					}
					break;
			}
		}

		if (firstCharacterUppercase) {
			sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));
		}

		return sb.toString();
	}

}
