





package net.mingsoft.mdiy.util;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.map.MapWrapper;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import freemarker.cache.FileTemplateLoader;
import freemarker.cache.MultiTemplateLoader;
import freemarker.cache.StringTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.core.ParseException;
import freemarker.core.TemplateClassResolver;
import freemarker.template.*;
import net.mingsoft.base.constant.Const;
import net.mingsoft.base.exception.BusinessException;
import net.mingsoft.base.util.BundleUtil;
import net.mingsoft.base.util.SqlInjectionUtil;
import net.mingsoft.basic.util.BasicUtil;
import net.mingsoft.basic.util.SpringUtil;
import net.mingsoft.basic.util.StringUtil;
import net.mingsoft.config.MSProperties;
import net.mingsoft.mdiy.biz.ITagBiz;
import net.mingsoft.mdiy.constant.e.TagTypeEnum;
import net.mingsoft.mdiy.entity.TagEntity;
import net.mingsoft.mdiy.service.BaseTagClassService;
import net.mingsoft.mdiy.tag.CustomTag;
import net.mingsoft.mdiy.tag.IncludeExTag;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.sql.Clob;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParserUtil {
    /*
     * log4j日志记录
     */
    protected final static Logger LOG = LoggerFactory.getLogger(ParserUtil.class);

    /**
     * 是否开启短链
     */
    public static final String SHORT_SWITCH = "shortSwitch";

    /**
     * 静态文件生成路径;例如：mcms/html/1
     */
    public static final String HTML = "html";

    /**
     * index
     */
    public static final String INDEX = "index";

    /**
     * 文件夹路径名后缀;例如：1/58/71.html
     */
    public static final String HTML_SUFFIX = ".html";

    /**
     * 标签指令前缀
     */
    public static final String TAG_PREFIX = "ms_";

    /**
     * 生成的静态列表页面名;例如：list1.html
     */
    public static final String PAGE_LIST = "list-";
    /**
     * 模板文件后缀名;例如：index.htm
     */
    public static final String HTM_SUFFIX = ".htm";

    /**
     * 是否是动态解析;true:动态、false：静态
     */
    public static final String IS_DO = "isDo";

    /**
     * 当前系统访问路径
     */
    public static final String URL = "url";

    /**
     * 栏目实体;
     */
    public static final String COLUMN = "column";

    /**
     * 文章编号
     */
    public static final String ID = "id";

    /**
     * 字段名称
     */
    public static final String FIELD = "field";

    /**
     * 作用域变量名称，避免共享变量影响标签数据展示 值范围 true|false
     * 使用场景：如 搜索页 两个arclist，一个展示搜索结果，一个展示热门数据，当搜索场景为指定栏目id并搜索模型字段值时默认会影响热门数据，通过scope=true避免被影响
     */
    public static final String SCOPE = "scope";


    /**
     * 自定义模型表名;
     */
    public static final String TABLE_NAME = "tableName";

    /**
     * 搜索时自定义模型表名变量 避免相同变量 影响栏目模型
     */
    public static final String SEARCH_TABLE_NAME = "searchTableName";

    /**
     * 模块路径;
     */
    public static final String MODEL_NAME = "modelName";


    /**
     * 分页，提供給解析传递给sql解析使用
     */
    public static final String PAGE = "pageTag";


    /**
     * 栏目编号;原标签没有使用驼峰命名
     */
    public static final String TYPE_ID = "typeid";


    /**
     * 站点编号
     */
    public static final String APP_ID = "appId";

    /**
     * 站点目录
     */
    public static final String APP_DIR = "appDir";

    /**
     * 模板文件夹
     */
    public static final String TEMPLATE = "template";



    /**
     * 初始化Configuration
     */
    public static Configuration cfg = new Configuration(Configuration.VERSION_2_3_0);

    /**
     * 文件模板渲染
     */
    public static FileTemplateLoader ftl = null;

    /**
     * 字符串模板渲染
     */
    public static StringTemplateLoader stringLoader;


    /**
     * 系统预设需要特殊条件的标签
     */
    public static List<String> systemTag1 = CollUtil.toList("field", "pre", "page", "next");


    /**
     * 线程锁，Configuration的共享变量不是线程安全的，这会导致在执行列表标签的时候会执行到重复的sql，因此需要给方法上锁
     */
    public static final Lock LOCK = new ReentrantLock();

    /**
     * 获取模板文件夹
     * @return ms.upload.template + 应用编号
     */
    public static String buildTemplatePath() {
        return ParserUtil.buildTemplatePath(null);
    }

    /**
     * 拼接模板文件路径
     *
     * @param path 主题下对应的htm模板文件
     * @return 完整的模板文件路径
     */
    public static String buildTemplatePath(String path) {
        return ParserUtil.buildTemplatePath(null,path);
    }

    /**
     * 上下文路径
     */
    public static final String CONTEXT_PATH = "contextPath";

    /**
     * 预设标签检测变量
     * key 变量名称
     * value 检测类型 TABLE_COLUMN_CHECK（正则检测 针对表名、字段名） SINGLE_QUOTE_CHECK
     */
    public static final Map<String,String> CHECK_VARIABLE_TYPES = new HashMap<>();

    /**
     * 表名、字段名 检测类型
     */
    public static final String TABLE_COLUMN_CHECK = "TABLE_COLUMN_CHECK";

    /**
     * 单引号' 检测，场景 freemarker sql模板 '${xxx}' 避免值传入' 破坏预期结果
     */
    public static final String SINGLE_QUOTE_CHECK = "SINGLE_QUOTE_CHECK";

    static {
        CHECK_VARIABLE_TYPES.put("tableName", TABLE_COLUMN_CHECK);
        CHECK_VARIABLE_TYPES.put("orderby", TABLE_COLUMN_CHECK);

        CHECK_VARIABLE_TYPES.put("modelName",SINGLE_QUOTE_CHECK);
        CHECK_VARIABLE_TYPES.put("templateName",SINGLE_QUOTE_CHECK);
        CHECK_VARIABLE_TYPES.put("contextPath",SINGLE_QUOTE_CHECK);
    }

    /**
     * 更具指定皮肤生成模板
     * @param style 指定主题获取模板
     * @param path 主题下对应的htm模板文件
     * @return
     */
    public static String buildTemplatePath(String style,String path) {
        String uploadTemplatePath = MSProperties.upload.template;
        if (BasicUtil.getWebsiteApp() != null) {
            return BasicUtil.getRealPath(uploadTemplatePath + File.separator + BasicUtil.getWebsiteApp().getAppId() + File.separator
                    + (style != null ? (File.separator + style) : BasicUtil.getWebsiteApp().getAppStyle() ) + (path != null ? (File.separator + path) : ""));
        } else {
            return BasicUtil.getRealPath(uploadTemplatePath + File.separator + BasicUtil.getApp().getAppId() + File.separator
                    + (style != null ? (File.separator + style) : BasicUtil.getApp().getAppStyle()) + (path != null ? (File.separator + path) : ""));
        }
    }


    /**
     * 拼接生成后的路径地址
     * @param path 当前业务路径
     * @param appDir 站点路径，根据应用设置配置
     * @param htmlDir 静态文件根路径，根据yml配置，默认html
     * @return
     */
    public static String buildHtmlPath(String path,String htmlDir,String appDir) {
        return BasicUtil.getRealPath(htmlDir) + File.separator + appDir + File.separator + path
                + HTML_SUFFIX;
    }


    /**
     * 根据文本内容渲染模板
     *
     * @param root    参数值
     * @param content 模板内容
     * @return 渲染后的内容
     */
    public static String rendering(Map root, String content) throws IOException, TemplateException {
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_0);
        StringTemplateLoader stringLoader = new StringTemplateLoader();
        stringLoader.putTemplate("template", content);
        cfg.setNewBuiltinClassResolver(TemplateClassResolver.ALLOWS_NOTHING_RESOLVER);
        cfg.setNumberFormat("#");
        cfg.setTemplateLoader(stringLoader);
        cfg.setSharedVariable("MUtil", new MUtil());
        Template template = cfg.getTemplate("template", "utf-8");
        StringWriter writer = new StringWriter();
        template.process(root, writer);
        return writer.toString();

    }


    /**
     * 根据模板文件渲染
     *
     * @param templatePath 模板路径
     * @return
     * @throws TemplateNotFoundException
     * @throws MalformedTemplateNameException
     * @throws ParseException
     * @throws IOException
     */
    public static int getPageSize(String templatePath, int defaultSize) {
        //组织模板路径
        String buildTempletPath = ParserUtil.buildTemplatePath();
        // 判断路径是否合法且是否存在
        String filePath = ParserUtil.isInvalidFileNameAndIsExist(buildTempletPath, templatePath);
        // 读取模板文件
        String content = FileUtil.readString(filePath, CharsetUtil.CHARSET_UTF_8);

        // 创建 Pattern 对象
        Pattern pattern = Pattern.compile("\\{(.*?)ispaging=true(.*?)\\}");
        // 现在创建 matcher 对象
        Matcher m = pattern.matcher(content);

        String size = null;
        if (m.find()) {
            size = ReUtil.extractMulti("size=(\\d*)", m.group(1), "$1");
            //没有找到继续找
            if (size == null) {
                size = ReUtil.extractMulti("size=(\\d*)", m.group(2), "$1");
            }

            if (size != null) {
                defaultSize = Integer.parseInt(size);
            }
            LOG.debug("获取分页的size:{}", size);
        }

        return defaultSize;
    }


    /**
     * 渲染模板
     *
     * @param templatePath 模板路径
     * @param map          传入参数
     * @return
     * @throws TemplateNotFoundException
     * @throws MalformedTemplateNameException
     * @throws ParseException
     * @throws IOException
     */
    public static String rendering(String templatePath, Map<String,Object> map)
            throws TemplateNotFoundException, MalformedTemplateNameException, ParseException, IOException {
        // todo 上锁
        LOCK.lock();
        try {
            //组织模板路径
            String buildTempletPath = ParserUtil.buildTemplatePath();
            //读取标签
            ITagBiz tagBiz = SpringUtil.getBean(ITagBiz.class);
            List<TagEntity> list = tagBiz.list();
            //自动导入宏
            ClassPathResource classPathResource = new ClassPathResource("WEB-INF/macro.ftl");
            StringBuffer sb = new StringBuffer(IOUtils.toString(classPathResource.getInputStream(), "UTF-8"));
            //初始化
            if (ftl == null || !buildTempletPath.equals(ftl.baseDir.getPath())) {

                stringLoader = new StringTemplateLoader();
                ftl = new FileTemplateLoader(new File(buildTempletPath));
                MultiTemplateLoader multiTemplateLoader = new MultiTemplateLoader(new TemplateLoader[]{stringLoader, ftl});
                cfg.setNewBuiltinClassResolver(TemplateClassResolver.ALLOWS_NOTHING_RESOLVER);
                cfg.setNumberFormat("#");
                cfg.setTemplateLoader(multiTemplateLoader);
                cfg.setSharedVariable("MUtil", new MUtil());
                cfg.setClassicCompatible(true);
                cfg.addAutoInclude("macro.ms");
            }

            //读取自定义宏
            list.forEach(tag -> {
                TagTypeEnum typeEnum = TagTypeEnum.get(tag.getTagType());
                if (typeEnum == TagTypeEnum.MACRO) {//列表标签
                    sb.append(tag.getTagSql());
                }
            });
            stringLoader.putTemplate("macro.ms", sb.toString());
            // 读取模板文件
            // 判断路径是否合法且是否存在
            String filePath = ParserUtil.isInvalidFileNameAndIsExist(buildTempletPath, templatePath);
            String temp = FileUtil.readString(filePath, CharsetUtil.CHARSET_UTF_8);

            //获取自定义模板
            Template template = null;
            //替换标签
            temp = replaceTag(temp);
            //添加自定义模板
            stringLoader.putTemplate("ms:custom:" + templatePath, temp);

            try {
                template = cfg.getTemplate("ms:custom:" + templatePath, Const.UTF8);
            } catch (Exception e) {
                LOG.debug("模板错误");
                e.printStackTrace();
                LOG.debug(temp);
            }

            //设置兼容模式
            cfg.setClassicCompatible(true);
            //设置扩展include
            cfg.setSharedVariable(TAG_PREFIX + "includeEx", new IncludeExTag(buildTempletPath, stringLoader));

            // 扁平化处理map提供sql预编译参数
            Map<String,Object> sqlPrepareParams = flatten(map);

            list.forEach(tag -> {
                //添加自定义标签
                if (StrUtil.isNotBlank(tag.getTagName())) {

                    TagTypeEnum typeEnum = TagTypeEnum.get(tag.getTagType());

                    if (typeEnum == TagTypeEnum.LIST) {//列表标签
                        cfg.setSharedVariable(TAG_PREFIX + tag.getTagName(), new CustomTag(map, tag));
                    }

                    // TODO: 2024/11/19 增加tag判断是为了处理全局标签处理，其余内容都是类似的，所以放在一起判断了 
                    if ((typeEnum == TagTypeEnum.SINGLE || typeEnum == TagTypeEnum.GLOBAL) && (!systemTag1.contains(tag.getTagName())
                            //文字内容需要id参数
                            || (map.containsKey("id") && tag.getTagName().equals("field"))
                            //分页需要pageTag参数
                            || (map.containsKey("pageTag") && (tag.getTagName().equals("pre")
                            || tag.getTagName().equals("next") || tag.getTagName().equals("page")))
                    )) {
                        if (StrUtil.isNotEmpty(tag.getTagClass())) {
                            BaseTagClassService baseTagClassService = (BaseTagClassService) SpringUtil.getBean(tag.getTagClass());
                            Object obj = baseTagClassService.excute(map);
                            if (obj != null) {
                                map.put(tag.getTagName(), obj);
                            }
                        } else {
                            String sql = null;
                            try {
                                sql = rendering(map, tag.getTagSql());
                                List<Map<String, Object>> _list = tagBiz.queryForListByNamedJdbc(sql,sqlPrepareParams);
                                if (_list.size() > 0) {
                                    if (_list.get(0) != null) {
                                        MapWrapper<String, Object> mw = new MapWrapper<>(_list.get(0));
                                        mw.forEach(x -> {
                                            //把Clob类型转化成string
                                            if (x.getValue() instanceof Clob) {
                                                x.setValue(StringUtil.clobStr((Clob) x.getValue()));
                                            }
                                            // 处理数据库字段类型timestamp问题 文章content_datetime字段不再使用timestamp数据库类型
                                            // 使用datetime数据库类型，会处理成java的LocalDateTime会有T， 再转成java的Timestamp类型接收 方便格式化输出
                                            if (x.getValue() instanceof LocalDateTime){
                                                x.setValue(Timestamp.valueOf((LocalDateTime) x.getValue()));
                                            }
                                        });
                                    }
                                    // 合并数据，这里就可以变成可以存取多个数据，只要他们tagName一致
                                    // 那他们数据就可以叠加输出，增加了输出方式，减少文章和栏目自定义数据使用
                                    if (map.containsKey(tag.getTagName())) {
                                        // 获取新的Map数据
                                        Map<String, Object> dataMap = _list.get(0);
                                        // 如果有，就合并数据
                                        Object o = map.get(tag.getTagName());
                                        if (ObjectUtil.isNotNull(o)) {
                                            // 把之前的数据暂存
                                            Map tempMap = (Map) o;
                                            // 新的数据覆盖之前的数据
                                            tempMap.putAll(dataMap);
                                            map.put(tag.getTagName(), tempMap);
                                        }
                                    } else {
                                        map.put(tag.getTagName(), _list.get(0));
                                    }
                                }
                            } catch (IOException e) {
                                LOG.error("", e);
                            } catch (TemplateException e) {
                                LOG.error("", e);
                            }
                        }
                    }
                }
            });

            StringWriter writer = new StringWriter();
            template.process(map, writer);
            return writer.toString();
        } catch (Exception e) {
            LOG.error("渲染错误", e);
            e.printStackTrace();
        } finally {
            // TODO 解锁
            LOCK.unlock();
        }
        return null;
    }


    /**
     * 标签替换
     *
     * @param content 模板内容
     * @return 替换后的内容
     */
    public static String replaceTag(String content) {
        // 创建 Pattern 对象
        //替include标签 <#include "header.htm" /> 或者 <#include "header.htm">  转换为 <@ms_includeEx template=header.htm/>
        // template 模板文件夹名称 从配置中读取
        String templateFolder = ConfigUtil.getString("文件上传配置", "uploadTemplate",MSProperties.upload.template);
        content = content.replaceAll("<#include(.*)/>", StrUtil.format("<@{}includeEx {}=$1/>", TAG_PREFIX,templateFolder));
        content = content.replaceAll("<#include(.*)>", StrUtil.format("<@{}includeEx {}=$1/>", TAG_PREFIX,templateFolder));

        //替换全局标签{ms:global.name/} 转换为{global.name/}
        content = content.replaceAll("\\{ms:([^\\}]+)/\\}", "\\${$1}");
        //替换全局标签 {@ms:file */} 转换为<@ms_file */>
        content = content.replaceAll("\\{@ms:([^\\}]+)/\\}", StrUtil.format("<@{}$1/>", TAG_PREFIX));

        //替换列表开头标签 {ms:arclist *} 转换为{@ms_arclist */}
        content = content.replaceAll("\\{ms:([^\\}]+)\\}", StrUtil.format("<@{}$1>", TAG_PREFIX));
        //替换列表结束标签 {/ms:arclist *} 转换为{/@ms_arclist}
        content = content.replaceAll("\\{/ms:([^\\}]+)\\}", StrUtil.format("</@{}$1>", TAG_PREFIX));
        //替换内容老的标签 [field.*/] 转换为${filed.*}
        content = content.replaceAll("\\[([^\\]]+)/\\]", "\\${$1}");

        // 替换html中的注释 转换为<#noparse><!----></#noparse>
        content = content.replaceAll("(<!--[\\s\\S]*?-->)", "<#noparse>$1</#noparse>");

        return content;
    }

    /**
     * 拼接路径和文件名且会判断文件名是否合法且是否存在，如果无误则返回拼装后的路径
     * @param filePath 文件路径
     * @param fileName 文件名，不能以/开头。否则直接判断文件名不合法
     * @exception BusinessException 文件名不合法或文件不存在
     */
    public static String isInvalidFileNameAndIsExist(String filePath, String fileName) {
        if (net.mingsoft.basic.util.FileUtil.isInvalidFileName(fileName)) {
            LOG.debug("该文件名不合法：{}",fileName);
            throw new BusinessException(BundleUtil.getBaseString("err.error", BundleUtil.getString(net.mingsoft.basic.constant.Const.RESOURCES, "file.path")));
        }
        filePath = filePath + FileUtil.FILE_SEPARATOR + fileName;
        if (!FileUtil.exist(filePath)) {
            LOG.debug("该文件路径不存在：{}",filePath);
            throw new BusinessException(BundleUtil.getBaseString("err.not.exist", BundleUtil.getString(net.mingsoft.basic.constant.Const.RESOURCES, "file.path")));
        }
        // 确认存在则返回拼装后的路径
        return filePath;
    }


    /**
     * 扁平化处理 FreeMarker 渲染参数 Map。注意 此处的扁平转换逻辑和标签模板写法对应！
     * <p>
     * 该方法将复杂的嵌套结构转换为扁平的单层 Map，以适配 NamedParameterJdbcTemplate 的预编译占位符。
     *
     * <h3>处理场景说明：</h3>
     * <ul>
     *   <li><b>场景 1：嵌套 Map</b>
     *     <br>输入：<code>{ "search": { "title": "java" } }</code>
     *     <br>输出 Key：使用下划线连接。如 <code>search_title -> "java"</code>
     *   </li>
     *   <li><b>场景 2：net.mingsoft包对象（如CategoryEntity,CategoryBean）</b>
     *      *     <br>背景：FreeMarker 模板中常使用 <code>column.id,column.categoryParentIds</code> 从对象中取值的场景。
     *      *     <br>输入：<code>{ "column": CategoryEntity对象 }</code>
     *      *     <br>输出 Key：
     *      *     <br> - 原始值：<code>column -> CategoryEntity</code>
     *      *     <br> - 对象值：<code>column_id -> CategoryEntity.id</code>, <code>column_categoryParentIds -> CategoryEntity.categoryParentIds</code>
     *      *   </li>
     *   <li><b>场景 3：逗号分隔的字符串（核心适配场景）</b>
     *     <br>背景：FreeMarker 模板中常使用 <code>val?split(',')</code> 进行循环生成占位符。
     *     <br>输入：<code>{ "tags": "A,B,C" }</code>
     *     <br>输出 Key：
     *     <br> - 原始值：<code>tags -> "A,B,C"</code>
     *     <br> - 索引值：<code>tags_0 -> "A"</code>, <code>tags_1 -> "B"</code>, <code>tags_2 -> "C"</code>
     *   </li>
     *   <li><b>场景 4：集合或数组（Iterable）</b>
     *     <br>输入：<code>{ "ids": [10, 20] }</code>
     *     <br>输出 Key：
     *     <br> - 索引值：<code>ids_0 -> 10</code>, <code>ids_1 -> 20</code>
     *     <br> - 原始值：<code>ids -> [10, 20]</code> (供 NamedJdbc 的 IN 语法使用)
     *   </li>
     *   <li><b>场景 5：多级深度嵌套</b>
     *     <br>输入：<code>{ "sys": { "user": { "id": 1 } } }</code>
     *     <br>输出 Key：递归连接。如 <code>sys_user_id -> 1</code>
     *   </li>
     * </ul>
     *
     * @param source 原始嵌套 Map（通常为 FreeMarker 渲染时使用的 root map）
     * @return 扁平化后的 Map，Key 为符合预编译命名的字符串，Value 为对应的参数值
     */
    public static Map<String, Object> flatten(Map<String, Object> source) {
        Map<String, Object> result = new HashMap<>();
        if (source == null) return result;

        buildFlatMap("", source, result);
        return result;
    }


    /**
     * 按照值格式 检测参数
     * @param params 参数map
     */
    public static void checkRequestParams (Map<String,Object> params) {
        for (var rule : CHECK_VARIABLE_TYPES.entrySet()) {
            String key = rule.getKey();

            Object rawValue = params.get(key);
            if (ObjectUtil.isEmpty(rawValue)) {
                continue;
            }
            String value = rule.getValue();

            String paramValue = Convert.toStr(rawValue);
            if (TABLE_COLUMN_CHECK.equals(value)) {
                String[] regParams = new String[]{paramValue};
                if ("orderby".equals(key)) {
                    regParams = paramValue.split(",");
                }
                SqlInjectionUtil.checkStandardTableColumnName(regParams);
            } else if (SINGLE_QUOTE_CHECK.equals(value) && paramValue.contains("'")){
                LOG.warn("参数 [ {}:{} ] 含有非法单引号！",key,paramValue);
                throw new BusinessException("安全拦截：参数不合法");
            }
        }
    }


    private static void buildFlatMap(String prefix, Object value, Map<String, Object> result) {
        if (value instanceof Map<?, ?> map) {
            // 递归处理嵌套 Map
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                String newPrefix = prefix.isEmpty() ? entry.getKey().toString() : prefix + "_" + entry.getKey();
                buildFlatMap(newPrefix, entry.getValue(), result);
            }
        } else if (value != null && value.getClass().getName().contains("net.mingsoft")) {
            // 处理对象属性获取
            Map<String, Object> beanMap = BeanUtil.beanToMap(value, false, false);
            for (Map.Entry<String, Object> entry : beanMap.entrySet()) {
                String newPrefix = prefix.isEmpty() ? entry.getKey() : prefix + "_" + entry.getKey();
                buildFlatMap(newPrefix, entry.getValue(), result);
            }
        } else if (value instanceof Iterable<?> iterable) {
            // 处理集合/列表，生成 :key_0, :key_1
            int i = 0;
            for (Object item : iterable) {
                result.put(prefix + "_" + (i++), item);
            }
            // 同时也把原始列表存进去，方便 NamedJdbc 的 IN (:keys) 语法
            result.put(prefix, value);
        } else if (value instanceof String str && ((String) value).contains(",")) {
            // 核心逻辑：处理逗号分隔的字符串（对应你模板里的 ?split(',')）
            String[] parts = str.split(",");
            // find_in_set参数需要
            for (int i = 0; i < parts.length; i++) {
                result.put(prefix + "_" + i, parts[i].trim());
            }

            // 保留逗号分割字符串的List集合 in参数需要
            result.put(prefix, Arrays.asList(parts));
        } else {
            // 基本类型直接存入
            if (!prefix.isEmpty()) {
                result.put(prefix, value);
            }
        }
    }

}
