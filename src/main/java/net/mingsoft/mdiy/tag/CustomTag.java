



package net.mingsoft.mdiy.tag;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.map.MapWrapper;
import freemarker.core.Environment;
import freemarker.ext.beans.BeansWrapper;
import freemarker.ext.beans.BeansWrapperBuilder;
import freemarker.template.*;
import net.mingsoft.basic.util.SpringUtil;
import net.mingsoft.basic.util.StringUtil;
import net.mingsoft.mdiy.biz.ITagBiz;
import net.mingsoft.mdiy.entity.TagEntity;
import net.mingsoft.mdiy.util.ParserUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.Clob;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Description: 自定义标签
 * @Date: Create in 2020/06/23 9:16
 */

public class CustomTag implements TemplateDirectiveModel {
    protected static BeansWrapper build = new BeansWrapperBuilder(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS).build();
    /*
     * log4j日志记录
     */
    protected final Logger LOG = LoggerFactory.getLogger(this.getClass());

    /**
     * 解析标签用的map
     */
    private Map<String,Object> data;
    /**
     * 标签sql模板
     */
    private TagEntity tag;
    /**
     * 传出的变量名
     */
    private String variableName;



    private Map tagParams;


    public CustomTag() {

    }
    public CustomTag(Map<String,Object> data, TagEntity tag) {
        this.data = data;
        this.tag = tag;
        this.variableName = "field";
    }

    public Map getTagParams() {
        return this.tagParams;
    }

    @Override
    public void execute(Environment environment, Map params, TemplateModel[] templateModels,
                        TemplateDirectiveBody templateDirectiveBody) throws TemplateException, IOException {

        //如果只是获取标签属性
        if(data==null && tag == null) {
            this.tagParams = params;
            return;
        }

        HashMap<String, Object> _params = MapUtil.newHashMap();
        //历史变量
        //将外部传入的压入
        _params.putAll(data);

        TemplateModel oldVar = environment.getVariable(variableName);
        //压入了栏目则传入sql模板
        TemplateModel column = environment.getVariable(ParserUtil.COLUMN);
        if (column != null) {
            _params.put(ParserUtil.COLUMN, build.unwrap(column));
        }

        //将标签传入参数逐一的压入
        params.forEach((k, v) -> {
            if (v instanceof TemplateModel) {
                try {
                    _params.putIfAbsent((String) k, build.unwrap((TemplateModel) v));
                } catch (TemplateModelException e) {
                    e.printStackTrace();
                    LOG.error("转换参数错误 key:{} -{}", k, e);
                }
            }
        });

        // 是否仅使用指令参数
        boolean isScoped = false;
        if (params.containsKey(ParserUtil.SCOPE) && params.get(ParserUtil.SCOPE) instanceof TemplateBooleanModel scoped) {
            isScoped = scoped.getAsBoolean();
        }
        ITagBiz tagSqlBiz = SpringUtil.getBean(ITagBiz.class);
        String sql = "";
        if (isScoped) {
            sql = ParserUtil.rendering(params, tag.getTagSql());
        } else {
            sql = ParserUtil.rendering(_params, tag.getTagSql());
        }

        // 扁平化处理map提供sql预编译参数
        Map<String, Object> sqlPrepareParams = ParserUtil.flatten(_params);
        List<Map<String,Object>> _list = tagSqlBiz.queryForListByNamedJdbc(sql,sqlPrepareParams);

        AtomicInteger index = new AtomicInteger(1);
        //渲染标签
        _list.forEach(x -> {
            //实现索引
            x.put("index", index.getAndIncrement());
            try {
                MapWrapper<String, Object> mw = new MapWrapper<>(x);
                mw.forEach(y-> {
                    //把Clob类型转化成string
                    if (y.getValue() instanceof Clob) {
                        y.setValue(StringUtil.clobStr((Clob) y.getValue()));
                    }
                    // 处理数据库字段类型timestamp问题 文章content_datetime字段不再使用timestamp数据库类型
                    // 使用datetime数据库类型，会处理成java的LocalDateTime会有T， 再转成java的Timestamp类型接收 方便格式化输出
                    if (y.getValue() instanceof LocalDateTime){
                        y.setValue(Timestamp.valueOf((LocalDateTime) y.getValue()));
                    }
                });

                TemplateModel columnModel = build.wrap(x);
                //如果自定义了变量则赋值自定义变量
                if (templateModels.length > 0) {
                    templateModels[0] = columnModel;
                } else {
                    environment.setVariable(variableName, columnModel);
                }

                //压入 以便嵌套使用
                environment.setVariable(ParserUtil.COLUMN, columnModel);
                //渲染
                templateDirectiveBody.render(environment.getOut());
                //渲染完成还原变量
                environment.setVariable(ParserUtil.COLUMN, column);
                environment.setVariable(variableName, oldVar);

            } catch (TemplateModelException e) {
                e.printStackTrace();
            } catch (TemplateException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }


}
