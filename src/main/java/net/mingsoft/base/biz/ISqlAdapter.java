




package net.mingsoft.base.biz;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import net.mingsoft.base.exception.BusinessException;
import net.mingsoft.base.util.SqlInjectionUtil;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 基础条件处理
 */
public interface ISqlAdapter {

    Logger LOG = LoggerFactory.getLogger(ISqlAdapter.class);

    /**
     * 对于带分页的标签sql，通过子查询包裹返回总数查询sql
     * @param pageSql 分页标签sql eg: select * from cms_content limit 10
     * @return 总数sql eg: select count(*) from (select * from cms_content) count_sql
     * @throws JSQLParserException sql解析异常
     */
    default String getCountSql(String pageSql) throws JSQLParserException {
        // 移除空行 避免因空行触发sql解析异常
        pageSql = pageSql.replaceAll("\\n{2,}", "\n");
        Select select = (Select) CCJSqlParserUtil.parse(pageSql);
        PlainSelect selectBody = select.getPlainSelect();

        // 移除分页
        selectBody.setLimit(null);

        // count子查询
        return "SELECT COUNT(*) FROM (" + selectBody + ") COUNT_SQL";
    }


    /**
     * 自增长id处理
     * @param tableName 表名
     * @return 自增长sql
     */
    default String getAutoIdSql(String tableName) {
        return "";
    }

    /**
     * 处理相等于SQL拼接
     *
     * @param sb       拼接对象
     * @param sqlWhere 条件对象
     */
    default void handleEq(StringBuffer sb, SqlQueryWrapper.SqlWhere sqlWhere) {
        // 增加查询字段名
        sb.append(sqlWhere.getField());
        sb.append(" = ?");
    }

    /**
     * 处理相不等于SQL拼接
     * @param sb       拼接对象
     * @param sqlWhere 条件对象
     */
    default void handleNe(StringBuffer sb, SqlQueryWrapper.SqlWhere sqlWhere) {
        // 增加查询字段名
        sb.append(sqlWhere.getField());
        sb.append(" != ?");
    }

    /**
     * 预处理大于SQL拼接
     *
     * @param sb       拼接对象
     * @param sqlWhere 条件对象
     */
    default void handleGt(StringBuffer sb, SqlQueryWrapper.SqlWhere sqlWhere) {
        // 获取类型
        String type = sqlWhere.getType();
        String field = sqlWhere.getField();
        if ("date".equals(type) || "time".equals(type)) {
            if ("time".equals(type)) {
                sb.append(" DATE_FORMAT(").append(field)
                        .append(", '%T')").append(" > ").append("DATE_FORMAT(?, '%T')");
            }
            if ("date".equals(type)) {
                sb.append(" DATE_FORMAT(").append(field)
                        .append(", '%Y-%m-%d %H:%i:%s')").append(" > ").append("DATE_FORMAT(?, '%Y-%m-%d %H:%i:%s')");
            }
        } else {
            // 增加查询字段名
            sb.append(field);
            sb.append(" > ?");
        }
    }

    /**
     * 预处理大于或等于SQL拼接
     *
     * @param sb       拼接对象
     * @param sqlWhere 条件对象
     */
    default void handleGet(StringBuffer sb, SqlQueryWrapper.SqlWhere sqlWhere) {
        sb.append(sqlWhere.getField());
        sb.append(" >= ?");
    }

    /**
     * 预处理小于SQL拼接
     *
     * @param sb       拼接对象
     * @param sqlWhere 条件对象
     */

    default void handleLt(StringBuffer sb, SqlQueryWrapper.SqlWhere sqlWhere) {
        String type = sqlWhere.getType();
        String field = sqlWhere.getField();
        // 获取类型
        if ("date".equals(type) || "time".equals(type)) {
            if ("time".equals(type)) {
                sb.append(" DATE_FORMAT(").append(field)
                        .append(", '%T')").append(" < ").append("STR_TO_DATE(?, '%H:%i:%s')");
            }
            if ("date".equals(type)) {
                sb.append(" DATE_FORMAT(").append(field)
                        .append(", '%Y-%m-%d %H:%i:%s')").append(" < ").append("DATE_FORMAT(?, '%Y-%m-%d %H:%i:%s')");
            }
        } else {
            // 增加查询字段名
            sb.append(field);
            sb.append(" < ?");
        }
    }

    /**
     * 预处理小于或等于SQL拼接
     *
     * @param sb       拼接对象
     * @param sqlWhere 条件对象
     */
    default void handleLet(StringBuffer sb, SqlQueryWrapper.SqlWhere sqlWhere) {
        sb.append(sqlWhere.getField());
        sb.append(" <= ?");
    }

    /**
     * 包含情况 预处理模糊SQL拼接
     *
     * @param sb       拼接对象
     * @param sqlWhere 条件对象
     */
    default void handleLike(StringBuffer sb, SqlQueryWrapper.SqlWhere sqlWhere) {
        sb.append(sqlWhere.getField());
        sb.append(" LIKE ").append("CONCAT('%', ?, '%')");
    }

    /**
     * 不包含情况 预处理模糊SQL拼接
     *
     * @param sb       拼接对象
     * @param sqlWhere 条件对象
     */
    default void handleNotLike(StringBuffer sb, SqlQueryWrapper.SqlWhere sqlWhere) {
        String field = sqlWhere.getField();

        sb.append("(").append(field);
        sb.append(" NOT LIKE ").append("CONCAT('%', ?, '%')");

        // 获取类型
        String type = sqlWhere.getType();
        // 在不包含情况下查询出NULL值，null值也算不包含
        if (!"time".equals(type) && !"date".equals(type)){
            sb.append(" OR ").append(field).append(" IS NULL ");
        }
        sb.append(")");
    }

    /**
     * 预处理左模糊SQL拼接
     *
     * @param sb       拼接对象
     * @param sqlWhere 条件对象
     */
    default void handleLikeLeft(StringBuffer sb, SqlQueryWrapper.SqlWhere sqlWhere) {
        sb.append(sqlWhere.getField());
        sb.append(" LIKE ").append("CONCAT('%', ?)");
    }

    /**
     * 预处理右模糊SQL拼接
     *
     * @param sb       拼接对象
     * @param sqlWhere 条件对象
     */
    default void handleLikeRight(StringBuffer sb, SqlQueryWrapper.SqlWhere sqlWhere) {
        sb.append(sqlWhere.getField());
        sb.append(" LIKE ").append("CONCAT(?, '%')");
    }

    /**
     * 预处理IN SQL拼接
     * 注意！！ in 场景下 value的格式必须是,分隔的值 eg:value1,value2,value3
     * @param sb       拼接对象
     * @param sqlWhere 条件对象
     */
    default void handleIn(StringBuffer sb, SqlQueryWrapper.SqlWhere sqlWhere) {
        sb.append(sqlWhere.getField());
        String[] values = sqlWhere.getValue().toString().split(",");
        int valueSize = values.length;
        String placeholders = String.join(",", Collections.nCopies(valueSize, "?"));
        sb.append(" IN ").append("(").append(placeholders).append(")");
    }

    /**
     * 为空情况 预处理SQL拼接
     *
     * @param sb       拼接对象
     * @param sqlWhere 条件对象
     */
    default void handleEmpty(StringBuffer sb, SqlQueryWrapper.SqlWhere sqlWhere) {
        String field = sqlWhere.getField();
        sb.append(field);
        sb.append(" IS NULL ");
        // 获取类型
        String type = sqlWhere.getType();

        if (!"time".equals(type) && !"date".equals(type)){
            sb.append(" OR ").append(field).append(" = '' ");
        }
    }

    /**
     * 不为空情况 预处理SQL拼接
     *
     * @param sb       拼接对象
     * @param sqlWhere 条件对象
     */
    default void handleNotEmpty(StringBuffer sb, SqlQueryWrapper.SqlWhere sqlWhere) {
        String field = sqlWhere.getField();
        sb.append(field);
        sb.append(" IS NOT NULL ");
        // 获取类型
        String type = sqlWhere.getType();

        if (!"time".equals(type) && !"date".equals(type)){
            sb.append(" AND ").append(field).append(" != '' ");
        }
    }

    /**
     * 预处理范围SQL拼接
     *
     * @param sb       拼接对象
     * @param sqlWhere 条件对象
     */
    default void handleRange(StringBuffer sb, SqlQueryWrapper.SqlWhere sqlWhere) {
        // 获取类型
        String type = sqlWhere.getType();
        String field = sqlWhere.getField();
        if ("time".equals(type)) {
            sb.append(" DATE_FORMAT(").append(field)
                    .append(", '%T')").append(" BETWEEN ").append("STR_TO_DATE(?, '%H:%i:%s')")
                    .append(" AND STR_TO_DATE(?, '%H:%i:%s')");
        } else if ("date".equals(type)) {
            sb.append(" DATE_FORMAT(").append(field)
                    .append(", '%Y-%m-%d %H:%i:%s')").append(" BETWEEN ").append("DATE_FORMAT(?, '%Y-%m-%d %H:%i:%s')")
                    .append(" AND DATE_FORMAT(?, '%Y-%m-%d %H:%i:%s')");
        } else {
            // 如果不是时间和日期类型，则直接使用字段名和参数占位符
            sb.append(field);
            sb.append(" BETWEEN ? AND ?");
        }
    }

    /**
     *  预处分页SQL拼接
     * @param querySql 查询语句
     * @param params 查询参数，提供给统计总数使用
     * @return 总数
     */
    default int handlePage(StringBuffer querySql, int start, int size, List params) {
        String countSql = "SELECT COUNT(*) AS TOTAL FROM (" + querySql.toString() + ") AS sub";
        LOG.debug("countSql:{}", countSql);
        Map<String, Object> totalMap = null;
        if (ArrayUtil.isAllNotEmpty(params)) {
            totalMap = SpringUtil.getBean(JdbcTemplate.class).queryForMap(countSql, params.toArray());
        } else {
            totalMap = SpringUtil.getBean(JdbcTemplate.class).queryForMap(countSql);
        }


        querySql.append(" LIMIT ?, ?");
        params.add(start);
        params.add(size);
        return Integer.parseInt(totalMap.get("TOTAL").toString());
    }

    /**
     * 预处理降序SQL拼接
     * @param querySql 查询语句
     * @param column 字段名
     */
    default void handleOrderByDesc(StringBuffer querySql, String column) {
        boolean isOrderBy = querySql.toString().toLowerCase().contains("order by");
        if (!isOrderBy) {
            querySql.append(" ORDER BY ").append(column).append(" DESC");
        } else {
            querySql.append(", ").append(column).append(" DESC");
        }
    }

    /**
     * 预处理升序SQL拼接
     * @param querySql 查询语句
     * @param column 字段名
     */
    default void handleOrderByAsc(StringBuffer querySql, String column) {
        boolean isOrderBy = querySql.toString().toLowerCase().contains("order by");
        if (!isOrderBy) {
            querySql.append(" ORDER BY ").append(column).append(" ASC");
        } else {
            querySql.append(", ").append(column).append(" ASC");
        }
    };

    /**
     * 处理时间类型转换
     * @param type 类型
     * @return 根据适配返回后预处理的字符串,具体需要看具体数据库适配 </br>
     *      如果是oracle，那么返回为to_date(?, 'yyyy-mm-dd hh24:mi:ss')
     */
    default String handleTime(String type) {
        return "?";
    };

    /**
     * 添加字段sql语句拼接，如果fieldLength为0，则不添加长度。谨慎调用。
     * 如果有注释，适配类为DM、Oracle和PG会返回两条SQL,请用;分割执行
     * @param tableName 表名
     * @param map 字段信息  必须包含field,jdbcType,name三个字段
     *            field:字段名
     *            jdbcType:字段类型
     *            name:字段注释
     * @return 添加字段sql语句
     */
    default String handleAddColumn(String tableName, Map map) {
        String fieldName = MapUtil.getStr(map, "field");
        String fieldType = MapUtil.getStr(map, "jdbcType");
        String fieldLength = MapUtil.getStr(map, "length");
        if (StrUtil.hasBlank(tableName, fieldName, fieldType)) {
            throw new BusinessException("参数不能为空");
        }
        // 防注入校验， fieldLength它是数字，所以不通过表名校验
        SqlInjectionUtil.checkStandardTableColumnName(tableName, fieldName, fieldType);
        StringBuilder sql = new StringBuilder();
        sql.append("ALTER TABLE ").append(tableName).append(" ADD ").append(fieldName).append(" ").append(fieldType);
        if (StrUtil.isNotBlank(fieldLength) && !"0".equals(fieldLength)) {
            // 如果是decimal类型，则需要分割判断是否为数字类型
            if ("decimal".equalsIgnoreCase(fieldType)) {
                List<String> fieldLengthList = StrUtil.split(fieldLength, ",");
                if (CollUtil.isEmpty(fieldLengthList) || fieldLengthList.size() > 2) {
                    throw new BusinessException("字段长度格式错误");
                }
                for (String length : fieldLengthList) {
                    if (!NumberUtil.isInteger(length)) {
                        // 判断字段长度是否为数字
                        throw new BusinessException("字段长度只能为数字");
                    }
                }
            } else if (!NumberUtil.isInteger(fieldLength)) {
                // 判断字段长度是否为数字
                throw new BusinessException("字段长度只能为数字");
            }
            sql.append("(").append(fieldLength).append(")");
        }

        String fieldComment = handleFieldComment(tableName, map);
        // 添加字段注释
        if (StrUtil.isNotBlank(fieldComment)) {
            sql.append(fieldComment);
        }

        return sql.toString();
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
    default String handleModifyColumn(String tableName, Map map) {
        String fieldName = MapUtil.getStr(map, "field");
        String fieldType = MapUtil.getStr(map, "jdbcType");
        String fieldLength = MapUtil.getStr(map, "length");
        if (StrUtil.hasBlank(tableName, fieldName, fieldType)) {
            throw new BusinessException("参数不能为空");
        }
        // 防注入校验
        SqlInjectionUtil.checkStandardTableColumnName(tableName, fieldName, fieldType);
        StringBuilder sql = new StringBuilder();
        sql.append("ALTER TABLE ").append(tableName).append(" MODIFY ").append(fieldName).append(" ").append(fieldType);
        if (StrUtil.isNotBlank(fieldLength) && !"0".equals(fieldLength)) {
            // 如果是decimal类型，则需要分割判断是否为数字类型
            if ("decimal".equalsIgnoreCase(fieldType)) {
                List<String> fieldLengthList = StrUtil.split(fieldLength, ",");
                if (CollUtil.isEmpty(fieldLengthList) || fieldLengthList.size() > 2) {
                    throw new BusinessException("字段长度格式错误");
                }
                for (String length : fieldLengthList) {
                    if (!NumberUtil.isInteger(length)) {
                        // 判断字段长度是否为数字
                        throw new BusinessException("字段长度只能为数字");
                    }
                }
            } else if (!NumberUtil.isInteger(fieldLength)) {
                // 判断字段长度是否为数字
                throw new BusinessException("字段长度只能为数字");
            }
            sql.append("(").append(fieldLength).append(")");
        }

        String fieldComment = handleFieldComment(tableName, map);
        // 添加字段注释
        if (StrUtil.isNotBlank(fieldComment)) {
            sql.append(fieldComment);
        }
        return sql.toString();
    }

    /**
     * 增加字段注释sql语句拼接，不支持单独调用，请直接调用{@link #handleAddColumn(String, Map)}或者{@link #handleModifyColumn(String, Map)}使用
     * @param tableName 表名
     * @param map 字段信息
     * @return 增加字段注释sql语句
     */
    default String handleFieldComment(String tableName, Map map) {
        String fieldComment = MapUtil.getStr(map, "name");
        if (StrUtil.isBlank(fieldComment)) {
            return null;
        }
        SqlInjectionUtil.filterContent(fieldComment);
        return " COMMENT '" + fieldComment + "'";
    }

    /**
     * 查询数据中所有表
     * @return 数据库表集合
     */
    default List<String> queryTables(){
        String preSql = "SELECT table_name FROM information_schema.TABLES WHERE table_schema = (select database()) AND table_type = 'BASE TABLE'";
        List<Map<String, Object>> tableNameListMaps = SpringUtil.getBean(JdbcTemplate.class).queryForList(preSql);
        return  tableNameListMaps.stream().map(map -> map.get("table_name").toString()).collect(Collectors.toList());
    }


    /**
     * 查询指定表的所有字段列集合,字段名称的大小写根据表的字段决定
     * @param tableName 表名称
     * @return 表的字段列集合
     */
    default List<String> queryTableColumns(String tableName){
        String preSql = "SELECT column_name FROM information_schema.COLUMNS  WHERE TABLE_SCHEMA = (select database()) AND TABLE_NAME = ? ";
        List<Map<String, Object>> tableColumnsListMaps = SpringUtil.getBean(JdbcTemplate.class).queryForList(preSql,tableName);
        return  tableColumnsListMaps.stream().map(map -> map.get("column_name").toString()).collect(Collectors.toList());
    }
}
