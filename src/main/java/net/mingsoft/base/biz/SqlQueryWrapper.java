




package net.mingsoft.base.biz;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import net.sf.jsqlparser.JSQLParserException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;

import java.util.*;

/**
 * sql查询条件封装类
 */

public class SqlQueryWrapper {
    protected final Logger LOG = LoggerFactory.getLogger(this.getClass());

    /**
     * sql语句
     */
    protected StringBuffer querySql = new StringBuffer();

    /**
     * 当前查询语句的总数
     */
    private int querySqlTotal = 0;

    /**
     * 参数
     */
    protected List params = new ArrayList<>();

    /**
     * sqlAdapter
     */
    protected ISqlAdapter sqlAdapter;

    public String getQuerySql() {
        return querySql.toString();
    }

    public Object[] getParams() {
        return params.toArray();
    }

    /**
     * 构建无参使用方法
     * 方便调用其他数据库适配问题
     */
    public SqlQueryWrapper(){
        sqlAdapter = new ISqlAdapter() {
        };
    }

    /**
     * 构造方法，限制参数不能为空<br>
     * querySql中条件问号必须与params参数个数一致，否则出现执行语句失败
     * @param querySql 查询sql
     * @param params   多个参数,如果有params参数，那么 querySql 中必须存在对应个数的?占位符
     */
    public SqlQueryWrapper(@Nullable String querySql, @Nullable Object... params) {
        if (StrUtil.isNotBlank(querySql)) {
            this.querySql.append(querySql);
            //如果有参数就需要考虑是否拼接where条件
            boolean hasWhereClause = querySql.toLowerCase().contains("where");
            if (!hasWhereClause && ArrayUtil.isNotEmpty(params)) {
                this.querySql.append(" WHERE ");
            }
        }
        if (ArrayUtil.isNotEmpty(params)) {
            Collections.addAll(this.params, params);
        }
        sqlAdapter = new ISqlAdapter() {
        };

    }

    /**
     * @param sqlWhereList 高级筛选条件 示例：[{"action":"and","field":"qj_group","el":"like","model":"qjGroup","name":"任务组","type":"input","value":"1"}]
     *
     * @param sqlWhereList 高级筛选条件
     * @return SqlQueryWrapper
     */
    public SqlQueryWrapper sqlWhere(List<SqlWhere> sqlWhereList) {
        boolean hasWhereClause = querySql.toString().toLowerCase().contains("where");
        // 如果sql语句中没有where关键词且筛选条件不为空，则手动添加where
        if (!hasWhereClause && CollUtil.isNotEmpty(sqlWhereList)) {
            this.querySql.append(" WHERE ");
        }
        // 记录json中拼接次数
        int size = 0;
        // 取出条件拼接数据
        for (SqlWhere where : sqlWhereList) {

            // 获取字段名，下面拼接使用，防止多次取出
            String field = where.getField();
            // 获取值，下面拼接使用，防止多次取出
            Object value = where.getValue();
            // 获取拼接条件
            String action = ActionEnum.get(where.getAction()).getValue();

            // 任何一个条件为空，都跳出
            if (StringUtils.isBlank(field) || Objects.isNull(value)) {
                // 说明在唯一个判断中缺少条件，跳出，但是WHERE已添加，需要移除
                if (size == 0 && sqlWhereList.size() == 1 && !hasWhereClause) {
                    this.querySql.delete(this.querySql.length() - 7, this.querySql.length());
                }
                continue;
            }

            // 拼接判断语句，防止多拼接或者错误拼接
            if (hasWhereClause || size > 0) {
                querySql.append(" ").append(action).append(" ");
            }

            switch (ElEnum.get(where.getEl())) {
                case EQ:
                    sqlAdapter.handleEq(querySql, where);
                    params.add(value);
                    break;
                case NE:
                    sqlAdapter.handleNe(querySql, where);
                    params.add(value);
                    break;
                case GT:
                    sqlAdapter.handleGt(querySql, where);
                    params.add(value);
                    break;
                case GTE:
                    sqlAdapter.handleGet(querySql, where);
                    params.add(value);
                    break;
                case LT:
                    sqlAdapter.handleLt(querySql, where);
                    params.add(value);
                    break;
                case LTE:
                    sqlAdapter.handleLet(querySql, where);
                    params.add(value);
                    break;
                case LIKE:
                    sqlAdapter.handleLike(querySql, where);
                    params.add(value);
                    break;
                case NOTLIKE:
                    sqlAdapter.handleNotLike(querySql, where);
                    params.add(value);
                    break;
                case LIKELEFT:
                    sqlAdapter.handleLikeLeft(querySql, where);
                    params.add(value);
                    break;
                case LIKERIGHT:
                    sqlAdapter.handleLikeRight(querySql, where);
                    params.add(value);
                    break;
                case IN:
                    sqlAdapter.handleIn(querySql, where);
                    // 筛选传递的是数组
                    if (JSONUtil.isTypeJSONArray(String.valueOf(value))) {
                        List<String> inParams = Convert.toList(String.class, value);
                        params.addAll(inParams);
                    } else {
                        String[] valueArr = value.toString().split(",");
                        params.addAll(Arrays.asList(valueArr));
                    }
                    break;
                case EMPTY:
                    // 为空不需要外部值
                    sqlAdapter.handleEmpty(querySql, where);
                    break;
                case NOTEMPTY:
                    // 不为空不需要外部值
                    sqlAdapter.handleNotEmpty(querySql, where);
                    break;
                case RANGE:
                    JSONArray values = JSONUtil.parseArray(value);
                    params.add(values.getStr(0));
                    params.add(values.getStr(1));
                    sqlAdapter.handleRange(querySql, where);
                    break;
                default:
                    break;
            }

            size++;
        }

        return this;
    }

    /**
     * 高级筛选条件处理（JSON数组重载）<br>
     * 推荐使用 {@link #sqlWhere(List)} 方法替代
     * @param sqlWhere 高级筛选条件
     * @return SqlQueryWrapper
     */
    @Deprecated
    public SqlQueryWrapper sqlWhere(@Nullable JSONArray sqlWhere) {
        if (CollUtil.isEmpty(sqlWhere)) {
            return this;
        }
        return sqlWhere(sqlWhere.toList(SqlWhere.class));
    }

    /**
     * 高级筛选条件处理（字符串重载）
     *
     * @param sqlWhere 高级筛选条件
     * @return SqlQueryWrapper
     */
    public SqlQueryWrapper sqlWhere(String sqlWhere) {
        if (StringUtils.isEmpty(sqlWhere)) {
            return this;
        }
        return sqlWhere(JSONUtil.toList(sqlWhere, SqlWhere.class));
    }

    /**
     * 分页,在处理分页前需要计算当前查询的总数
     *
     * @param start 开始位置
     * @param size  显示数量
     */
    public void page(int start, int size) {

        //需要在组织分页前计算总数，否则会导致计算总数错误
        this.querySqlTotal = sqlAdapter.handlePage(querySql, start, size, this.params);
    }

    public String getCountSql(String pageSql) throws JSQLParserException {
        return sqlAdapter.getCountSql(pageSql);
    }

    /**
     * 根据表名获取自增sql
     * @param tableName 表名
     * @return 自增sql
     */
    public String getAutoIdSql(String tableName) {
        return sqlAdapter.getAutoIdSql(tableName);
    }

    /**
     * 根据字段名降序
     * @param column 字段名
     * @return
     */
    public SqlQueryWrapper orderByDesc(String column) {
        if (StringUtils.isBlank(column)) {
            return this;
        }
        sqlAdapter.handleOrderByDesc(querySql, column);
        return this;
    }

    /**
     * 根据字段名升序，如果有sqlWhere则需要在sqlWhere后
     * @param column 字段名
     * @return
     */
    public SqlQueryWrapper orderByAsc(String column) {
        if (StringUtils.isBlank(column)) {
            return this;
        }
        sqlAdapter.handleOrderByAsc(querySql, column);
        return this;
    }

    /**
     * 处理时间类型转换
     * @param type 类型
     * @return 根据适配返回后预处理的字符串,具体需要看具体数据库适配 </br>
     *      如果是oracle，那么返回为to_date(?, 'yyyy-mm-dd hh24:mi:ss')
     */
    public String handleTime(@Nullable String type) {
        return sqlAdapter.handleTime(type);
    }

    /**
     * 添加字段sql语句拼接，如果fieldLength为空或者为0，则不添加长度。谨慎调用。
     * 如果有注释，适配类为DM、Oracle和PG会返回两条SQL,请用;分割执行
     * @param tableName 表名
     * @param map 字段信息  必须包含field,jdbcType,name三个字段
     *            field:字段名
     *            jdbcType:字段类型
     *            name:字段注释
     * @return 添加字段sql语句
     */
    public String handleAddColumn(String tableName, Map map) {
        return sqlAdapter.handleAddColumn(tableName, map);
    }

    /**
     * 修改字段类型sql语句拼接，如果fieldLength为0，则不添加长度。谨慎调用。
     * 如果有注释，适配类为DM、Oracle和PG会返回两条SQL,请用;分割执行
     * @param tableName 表名
     * @param map 字段信息  必须包含field,jdbcType,name三个字段
     *            field:字段名
     *            jdbcType:字段类型
     *            name:字段注释
     * @return 修改字段类型sql语句拼接
     */
    public String handleModifyColumn(String tableName, Map map) {
        return sqlAdapter.handleModifyColumn(tableName, map);
    }

    /**
     * 返回总数
     *
     * @return 总数
     */
    public int getTotal() {
        return this.querySqlTotal;
    }

    /**
     * 查询数据中所有表
     * @return 数据库表集合
     */
    public List<String> queryTables(){
        return sqlAdapter.queryTables();
    }

    /**
     * 查询指定表的所有字段列集合,字段名称的大小写根据表的字段决定
     * @param tableName 表名称
     * @return 表的字段列集合
     */
    public List<String> queryTableColumns(String tableName){
        if (StrUtil.isBlank(tableName)){
            return Collections.emptyList();
        }
        return sqlAdapter.queryTableColumns(tableName);
    }

    /**
     * sqlWhere内部类
     * 方便存取数据
     */
    public class SqlWhere {
        /**
         * 操作符 and 或者 or, 默认值为and
         */
        private String action;
        /**
         * 数据库字段名
         */
        private String field;
        /**
         * 匹配值
         */
        private Object value;
        /**
         * 当前查询条件类型，如date,time,string
         */
        private String type;
        /**
         * 判断类型, 如eq, lt, gt, like这种,默认值为eq
         */
        private String el;
        /**
         * 是否是多选, 默认为false,以防有些时候手动拼接sqlWhere的时候，可能没有设置
         */
        private Boolean multiple = false;

        public String getAction() {
            return action;
        }

        public void setAction(String action) {
            this.action = action;
        }

        public String getField() {
            return field;
        }

        public void setField(String field) {
            this.field = field;
        }

        public Object getValue() {
            return value;
        }

        public void setValue(Object value) {
            this.value = value;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getEl() {
            return el;
        }

        public void setEl(String el) {
            this.el = el;
        }

        public Boolean getMultiple() {
            return multiple;
        }

        public void setMultiple(Boolean multiple) {
            this.multiple = multiple;
        }
    }

    /**
     * 操作符枚举类
     */
    public enum ActionEnum {
        /**
         * and
         */
        AND("AND"),
        /**
         * or
         */
        OR("OR");

        private String value;

        ActionEnum(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        /**
         * 判断传入的值是否存在,如果不存在,可能是恶搞,直接返回默认值and枚举(会忽略大小写)
         * @param value 判断的值
         * @return 相对应的枚举值, 默认值是and枚举
         */
        public static ActionEnum get(String value) {
            for (ActionEnum action : ActionEnum.values()) {
                if (action.getValue().equalsIgnoreCase(value)) {
                    return action;
                }
            }
            return ActionEnum.AND;
        }

    }

    /**
     * el表达式枚举类
     */
    public enum ElEnum {
        /**
         * 等于
         */
        EQ("eq"),
        /**
         * 不等于
         */
        NE("ne"),
        /**
         * 大于
         */
        GT("gt"),
        /**
         * 大于等于
         */
        GTE("gte"),
        /**
         * 小于
         */
        LT("lt"),
        /**
         * 小于等于
         */
        LTE("lte"),
        /**
         * 模糊查询
         */
        LIKE("like"),
        /**
         * 不包含
         */
        NOTLIKE("notLike"),
        /**
         * 左模糊查询
         */
        LIKELEFT("likeLeft"),
        /**
         * 右模糊查询
         */
        LIKERIGHT("likeRight"),
        /**
         * 包含查询
         */
        IN("in"),
        /**
         * 为空
         */
        EMPTY("empty"),
        /**
         * 不为空
         */
        NOTEMPTY("notEmpty"),
        /**
         * 范围条件查询
         */
        RANGE("range");

        private String value;

        ElEnum(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        /**
         * 判断传入的值是否存在,如果不存在,可能是恶搞,直接返回默认值eq枚举值(会忽略大小写)
         * @param value 判断的值
         * @return 相对应的枚举值, 默认值是eq枚举值
         */
        public static ElEnum get(String value) {
            for (ElEnum el : ElEnum.values()) {
                if (el.getValue().equalsIgnoreCase(value)) {
                    return el;
                }
            }
            return ElEnum.EQ;
        }
    }

    /**
     * 分页处理，封装返回对象
     */
    public static class EUListBean {
        private int total;

        private List rows;

        // 需要一个空的构造器防止fastJson初始化报错
        public EUListBean() {

        }

        public EUListBean(List rows, int total) {
            this.total = total;
            this.rows = rows;
        }


        public int getTotal() {
            return total;
        }


        public void setTotal(int total) {
            this.total = total;
        }


        public List getRows() {
            return rows;
        }


        public void setRows(List rows) {
            this.rows = rows;
        }
    }
}
