




package net.mingsoft.base.util;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.ParenthesedExpressionList;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.*;
import org.springframework.lang.Nullable;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * sql工具类
 * @author 铭软开发团队
 * @Description: 用于处理jdbcTemp的sql预处理语句，并转成可执行sql
 */
public class SqlUtil {

    private static final char MARK = '?';

    // 不需要拼接单引号的类型，这是筛选组件中type，因为sqlWhere中是没有JavaType
    private static final Set<String> NOT_NEED_BRACKETS;

    // 需要拼接单引号的类型，java.getClass()出来的类型
    private static final Set<String> NEED_BRACKETS;

    static {
        Set<String> types = new HashSet<>(1);
        // 目前就number不需要拼接，但是其他都需要
        types.add("number");
        NOT_NEED_BRACKETS = Collections.unmodifiableSet(types);

        // 需要拼接单引号的Java类型
        types = new HashSet<>(9);
        types.add("String");
        types.add("Date");
        types.add("Time");
        types.add("LocalDate");
        types.add("LocalTime");
        types.add("LocalDateTime");
        types.add("BigDecimal");
        types.add("Timestamp");
        types.add("DateTime");
        NEED_BRACKETS = Collections.unmodifiableSet(types);
    }

    /**
     * 命名占位符，与 Spring {@code NamedParameterJdbcTemplate} 的 {@code :name} 一致；
     * {@code :} 前不能为冒号或标识符字符，避免将 PostgreSQL {@code ::cast} 误识别为命名参数。
     */
    private static final Pattern NAMED_PARAM_PATTERN =
            Pattern.compile("(?<![:\\w]):([a-zA-Z_]\\w*)");

    /**
     * 把预处理的sql中的？替换成参数<br>
     * 废弃原因：无法满足sqlWhere值为空时查询，建议使用 {@link #parseSql(String, Object...)}<br>
     * 目前当前方法只在筛选组件中使用
     * @param sql 预处理sql
     * @param params 参数 Map<组件类型,组件值>如 {"number", 1},{"input", "1"} 建议通过SqlWhereWrapper.getParseParams()获取
     * @return 处理后的sql
     */
    @Deprecated
    public static StringBuilder parseSql(String sql, Queue<Map.Entry<String, Object>> params) {

        final StringBuilder sb = new StringBuilder(sql);

        for (int i = 0; i < sb.length(); i++) {
            if (sb.charAt(i) != MARK) {
                continue;
            }

            final Map.Entry<String, Object> param = params.poll();
            if (Objects.isNull(param)) {
                continue;
            }

            sb.deleteCharAt(i);

            if (NOT_NEED_BRACKETS.contains(param.getKey())) {
                sb.insert(i, param.getValue());
            } else {
                sb.insert(i, String.format("'%s'", param.getValue()));
            }

        }
        return sb;
    }

    /**
     * 把预处理的sql中的？替换成参数<br>
     * 其中会根据参数类型来拼接单引号，具体需要拼接类型请看{@link #NEED_BRACKETS}
     * @param sql 预处理sql
     * @param params 参数
     * @return 处理后的sql
     * 注意：当一些特殊参数传入时，会按照原格式输出。如new Date()展示出来时UTC格式，如果需要建议使用处理好格式时间类型，但是通过jdbcTemplate能执行成功
     */
    public static StringBuilder parseSql(String sql, @Nullable Object... params) {

        final StringBuilder sb = new StringBuilder(sql);

        if (Objects.isNull(params)) {
            return sb;
        }

        // 使用LinkedList因为可以插入null值，在params中会有null,需要正常展示
        Queue<Object> queue = new LinkedList<>();
        Collections.addAll(queue, params);

        for (int i = 0; i < sb.length(); i++) {
            if (sb.charAt(i) != MARK) {
                continue;
            }

            final Object param = queue.poll();
            sb.deleteCharAt(i);
            if (Objects.isNull(param)) {
                // 当传入空参时，需要更新为null
                sb.insert(i, "null");
            } else {
                if (NEED_BRACKETS.contains(param.getClass().getSimpleName())) {
                    sb.insert(i, String.format("'%s'", param));
                } else {
                    sb.insert(i, param);
                }
            }
        }
        return sb;
    }

    /**
     * 将命名参数（{@code :paramName}）SQL 与参数 Map 合并为便于日志查看的近似可执行 SQL。<br>
     * 仅用于调试输出；字符串按 SQL 字面量规则转义单引号；{@link #NEED_BRACKETS} 中的类型加引号；
     * 数值、布尔不加引号；{@link Collection} 展开为 {@code (a, b, c)}。未出现在 SQL 中的 map 键会被忽略。
     *
     * @param sql    含 {@code :name} 占位符的 SQL
     * @param params 参数 Map，键与占位符名一致（不含冒号）
     */
    public static StringBuilder parseSql(String sql, Map<String, Object> params) {
        if (Objects.isNull(sql)) {
            return new StringBuilder();
        }
        // 清理空行
        String cleanSql = sql.replaceAll("(?m)^[ \t]*\r?\n", "")
                .replaceAll("(\r?\n){2,}", "\n")
                .trim();
        if (Objects.isNull(params) || params.isEmpty()) {
            return new StringBuilder(cleanSql);
        }
        Matcher matcher = NAMED_PARAM_PATTERN.matcher(cleanSql);
        StringBuilder out = new StringBuilder();
        while (matcher.find()) {
            String name = matcher.group(1);
            Object value = params.get(name);
            matcher.appendReplacement(out, Matcher.quoteReplacement(formatNamedParamLiteral(value)));
        }
        matcher.appendTail(out);
        return out;
    }

    /**
     * 将命名的参数对象格式化为 SQL 字面量形式。
     * <p>
     * 逻辑说明：
     * 1. <b>空值处理</b>：null 转换为字符串 "null"；
     * 2. <b>集合处理</b>：若参数为 {@link Collection}，则递归展开并用逗号连接，包裹在圆括号内，如 (1, 2, 'A')；
     * 3. <b>引号包裹</b>：针对 {@link #NEED_BRACKETS} 中定义的类型（如 String, Date），在两端添加单引号，并转义内部单引号；
     * 4. <b>数值/布尔</b>：不添加引号，直接输出其字符串形式；
     * 5. <b>转义逻辑</b>：调用 {@link #escapeSqlStringLiteral} 确保生成的 SQL 文本符合数据库语法规范。
     *
     * @param param 待格式化的参数对象
     * @return 格式化后的 SQL 片段字符串
     */
    private static String formatNamedParamLiteral(Object param) {
        if (Objects.isNull(param)) {
            return "null";
        }
        if (param instanceof Collection<?> coll) {
            if (coll.isEmpty()) {
                return "(null)";
            }
            StringJoiner joiner = new StringJoiner(", ", "(", ")");
            for (Object o : coll) {
                joiner.add(formatNamedParamLiteral(o));
            }
            return joiner.toString();
        }
        String simpleName = param.getClass().getSimpleName();
        if (NEED_BRACKETS.contains(simpleName)) {
            return "'" + escapeSqlStringLiteral(param.toString()) + "'";
        }
        if (param instanceof Boolean || param instanceof Number) {
            return String.valueOf(param);
        }
        return String.valueOf(param);
    }

    /**
     * 转义字符串中的单引号以符合 SQL 字面量语法。以便生成的sql日志能正常执行
     * <p>
     * 按照标准 SQL 规范，字符串字面量中的一个单引号 (') 需要通过两个连续的单引号 ('') 来表示。
     *
     * <pre>
     * 示例：
     * raw = "O'Reilly" -> 返回 "O''Reilly"
     * raw = "1' OR '1'='1" -> 返回 "1'' OR ''1''=''1"
     * </pre>
     *
     * @param raw 原始字符串
     * @return 转义后的字符串；若输入为 null 则返回空字符串
     */
    private static String escapeSqlStringLiteral(String raw) {
        if (raw == null) {
            return "";
        }
        return raw.replace("'", "''");
    }

    /**
     * 通过JSqlParser 往原sql中添加条件 通过and进行连接
     * eg:
     *  sql: select id from cms_content where del = 0
     *  condition : create_by = '57' or update_by = '57'
     *  result: select id from cms_content where del = 0 and (create_by = '57' or update_by = '57')
     * @param sql 原执行sql
     * @param condition 预期添加的条件 不含逻辑连接关键字(and|or)
     * @return 添加条件后的sql
     */
    public static String addCondition(String sql,String condition){
        try {
            // TODO: 2025/7/11 多换行会导致解析结构丢失 常见于标签sql情况
            String formatSql = sql.replaceAll("\\n{2,}", "\n");
            // 通过JSqlParser解析sql  拦截器只允许了select 所以此处一定是select
            Statement statement = CCJSqlParserUtil.parse(formatSql);
            // 将数据权限条件处理成JSqlParser的条件表达式
            Expression dataScopeExpression = CCJSqlParserUtil.parseExpression(condition);
            // 条件添加括号
            ParenthesedExpressionList<Expression> expressions = new ParenthesedExpressionList<>(List.of(dataScopeExpression));
            // 当前sql
            Select select = (Select) statement;
            PlainSelect plainSelect = select.getPlainSelect();
            // 目标要修改条件的sql
            PlainSelect targetSelect = select.getPlainSelect();

            // TODO: 2025/7/10 这里有两种情况 1.执行原sql 2. pageHelper产生的select count sql,此时真实sql在子查询中
            boolean isPageHelperCountQuery = false;
            // 获取select查询的字段列
            List<SelectItem<?>> selectItems = plainSelect.getSelectItems();
            // 如果查询字段列只有一个且是count()函数 那么视为是pageHelper产生的count查询
            if (selectItems.size() == 1){
                Expression expression = selectItems.get(0).getExpression();
                if (expression instanceof Function function){
                    if ("COUNT".equalsIgnoreCase(function.getName())){
                        isPageHelperCountQuery = true;
                    }
                }
            }

            if (isPageHelperCountQuery) {
                // 从当前sql的from部分 获取到真实查询sql
                FromItem fromItem = plainSelect.getFromItem();
                if (fromItem instanceof ParenthesedSelect parenthesedSelect) {
                    targetSelect = parenthesedSelect.getSelect().getPlainSelect();
                }
            }

            // AND 拼接条件 修改原sql
            if (targetSelect.getWhere() != null){
                targetSelect.setWhere(new AndExpression(targetSelect.getWhere(), expressions));
            } else {
                targetSelect.setWhere(expressions);
            }
            return statement.toString();
        } catch (JSQLParserException e) {
            e.printStackTrace();
        }
        return sql;
    }


}
