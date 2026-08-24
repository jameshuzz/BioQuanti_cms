



package net.mingsoft.mdiy.biz.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.Dict;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import net.mingsoft.base.biz.SqlQueryWrapper;
import net.mingsoft.base.biz.impl.BaseBizImpl;
import net.mingsoft.base.dao.IBaseDao;
import net.mingsoft.base.exception.BusinessException;
import net.mingsoft.base.util.BundleUtil;
import net.mingsoft.base.util.SqlInjectionUtil;
import net.mingsoft.basic.entity.AppEntity;
import net.mingsoft.basic.util.BasicUtil;
import net.mingsoft.mdiy.bean.ModelJsonBean;
import net.mingsoft.mdiy.biz.IModelBiz;
import net.mingsoft.mdiy.constant.Const;
import net.mingsoft.mdiy.constant.e.ModelCustomTypeEnum;
import net.mingsoft.mdiy.dao.IModelDao;
import net.mingsoft.mdiy.entity.ModelEntity;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 自定义表单接口实现类
 *
 * @author 铭软
 * @version 版本号：100-000-000<br/>
 * 创建日期：2012-03-15<br/>
 * 历史修订：2022-1-21 modelDao.excuteSql(sql); 只有创建表和更新表可以执行
 * 历史修订：2023-12-12 重写base的一些sql执行方法; 方便对自定义相关操作做一些切面处理
 * 历史修订：2024-11-8 创建表和更新表使用excute方法，处理sql注入问题
 */
@Service("mdiyModelBizImpl")
@Transactional(rollbackFor = Exception.class)
public class ModelBizImpl extends BaseBizImpl<IModelDao, ModelEntity> implements IModelBiz {

    private static final Logger LOGGER = LoggerFactory.getLogger(ModelBizImpl.class);

    /**
     * 注入自定义表单持久化层
     */
    @Autowired
    private IModelDao modelDao;



    @Override
    protected IBaseDao getDao() {

        return modelDao;
    }

    @Override
    public boolean importConfig(String customType, ModelJsonBean modelJsonBean) {
        if (StringUtils.isEmpty(customType) || modelJsonBean == null) {
            return false;
        }
        return this.importModel(customType, modelJsonBean, "");
    }


    @Override
    public boolean importModel(String customType, ModelJsonBean modelJsonBean, String modelType) {
        if (StringUtils.isEmpty(customType) || modelJsonBean == null) {
            return false;
        }
        if (StringUtils.isBlank(modelJsonBean.getTitle())){
            return false;
        }

        String prefix = "MDIY_"+customType.toUpperCase()+"_";

        // 校验模型表名
        SqlInjectionUtil.checkStandardTableColumnName(prefix+modelJsonBean.getTableName());

        // 判断导入的模型业务类型一致的情况下，判断模型名 或 表名是否存在
        LambdaQueryWrapper<ModelEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(StringUtils.isNotBlank(customType),ModelEntity::getModelCustomType,customType)
                .and(wrapper -> wrapper.eq(ModelEntity::getModelName,modelJsonBean.getTitle())
                        .or().eq(StrUtil.isNotBlank(modelJsonBean.getTableName()),ModelEntity::getModelTableName,prefix+modelJsonBean.getTableName()));

        List<ModelEntity> modelEntities = this.list(queryWrapper);
        //判断表名是否存在
        if (CollectionUtil.isNotEmpty(modelEntities)) {
            LOGGER.debug("模型:{}已存在，请检查模型名称或者模型表名是否重复",modelJsonBean.getTitle());
            return false;
        }
        ModelEntity model = new ModelEntity();
        model.setModelName(modelJsonBean.getTitle());
        model.setModelTableName(prefix+modelJsonBean.getTableName());
        model.setModelCustomType(customType);
        model.setModelIdType(modelJsonBean.getId());

        executeModelSql(prefix,modelJsonBean);
        Map json = new HashMap();
        json.put("html", modelJsonBean.getHtml());
        json.put("searchJson", modelJsonBean.getSearchJson());
        json.put("script", modelJsonBean.getScript());
        json.put("isWebSubmit", modelJsonBean.isWebSubmit());
        json.put("isWebCode", modelJsonBean.isWebCode());
        json.put("id", modelJsonBean.getId());

        //因为ModelAop会进行站群插件id拼接，保存模型的时候需要还原最原始的表名称，方便复制JSON模型使用
        if (BasicUtil.getWebsiteApp() != null) {
            json.put("sql", modelJsonBean.getSql().replace("_" + BasicUtil.getWebsiteApp().getId(), ""));
            json.put("tableName", modelJsonBean.getTableName().replace("_" + BasicUtil.getWebsiteApp().getId(), ""));
        } else {
            json.put("tableName", modelJsonBean.getTableName());
            json.put("sql", modelJsonBean.getSql());
        }

        json.put("form", modelJsonBean.getForm());
        model.setModelField(modelJsonBean.getField());
        model.setModelType(modelType);
        model.setModelJson(JSONUtil.toJsonStr(json));
        model.setCreateDate(new Date());
        //保存自定义模型实体
        super.save(model);
        return true;
    }


    /**
     * 创建模型表
     * @param prefix 模型表前缀 MDIY_MODEL_|MDIY_FORM_
     * @param modelJsonBean 模型json
     */
    @Override
    public void executeModelSql(String prefix,ModelJsonBean modelJsonBean){
        //创建表
        if (StringUtils.isBlank(modelJsonBean.getSql())){
            LOGGER.debug("模型:{}的sql为空",modelJsonBean.getTitle());
            throw new BusinessException("模型异常，请重新拖拽模型导入");
        }
        List<String> sqlList = Arrays.stream(modelJsonBean.getSql()
                        .replace("{model}", prefix)
                        .trim()
                        .split(";")
                ).collect(Collectors.toList());
        String modelSql = sqlList.get(0); // 不考虑没有sql的情况 不考虑第一个不是create table或alter table的情况
        sqlList.remove(0);
        // 只允许创建、修改当前导入模型名称的表
        if (!StringUtils.containsAnyIgnoreCase(modelSql,"CREATE TABLE","ALTER TABLE")){
            LOGGER.warn("恶意篡改模型:{}的sql，ip:{},当前操作管理员:{}",modelJsonBean.getTitle(),BasicUtil.getIp(),BasicUtil.getManager().getManagerName());
            throw new BusinessException("模型异常，请重新拖拽模型导入");
        }
        String createRegex = "CREATE[\\s]*TABLE[\\s\\S]*"+ prefix + Pattern.quote(modelJsonBean.getTableName());
        String alterRegex = "ALTER[\\s]*TABLE[\\s\\S]*"+ prefix + Pattern.quote(modelJsonBean.getTableName());
        Pattern createPattern = Pattern.compile(createRegex, Pattern.CASE_INSENSITIVE);
        Pattern alterPattern = Pattern.compile(alterRegex, Pattern.CASE_INSENSITIVE);
        if (createPattern.matcher(modelSql).find() || alterPattern.matcher(modelSql).find()){
            SqlInjectionUtil.filterContent(new String[]{modelSql}, "CREATE","ALTER","(",")");
            CompletableFuture<Void> future = executeAsync(modelSql);
            try {
                future.get(3, TimeUnit.SECONDS);
            } catch (Exception e) {
                e.printStackTrace();
                LOGGER.error("模型:{}创建表异常",modelJsonBean.getTitle());
                throw new BusinessException("模型异常，请重新拖拽模型导入");
            }
            // 雪花id无额外操作
            if (modelJsonBean.getId() == 0){
                return;
            }
            // 自增长额外处理
            SqlQueryWrapper sqlQueryWrapper = new SqlQueryWrapper();
            String autoIdSql = sqlQueryWrapper.getAutoIdSql(prefix + modelJsonBean.getTableName());
            if (StringUtils.isNotBlank(autoIdSql)) {
                try {
                    future = executeAsync(autoIdSql);
                    future.get(3, TimeUnit.SECONDS);
                } catch (Exception e) {
                    e.printStackTrace();
                    LOGGER.error("执行:{}失败，处理模型:{}自增长Id异常，即将删除当前模型", autoIdSql,modelJsonBean.getTitle());
                    executeAsync("DROP TABLE " + prefix + modelJsonBean.getTableName());
                    throw new BusinessException("模型异常，请重新拖拽模型导入");
                }
            }
        }
        // 表、字段注释
        if (sqlList.size() > 0){
            String tableCommentRegex = "COMMENT\\s+ON\\s+TABLE\\s+\"?" + prefix + Pattern.quote(modelJsonBean.getTableName()) + "\"?";
            String columnCommentRegex = "COMMENT\\s+ON\\s+COLUMN\\s+\"?" + prefix + Pattern.quote(modelJsonBean.getTableName()) + "\"?.";
            Pattern tableCommentPattern = Pattern.compile(tableCommentRegex, Pattern.CASE_INSENSITIVE);
            Pattern columnCommentPattern = Pattern.compile(columnCommentRegex, Pattern.CASE_INSENSITIVE);
            List<CompletableFuture<Void>> futures = new ArrayList<>();
            sqlList.forEach(commentSql -> {
                if (tableCommentPattern.matcher(commentSql).find() || columnCommentPattern.matcher(commentSql).find()){
                    SqlInjectionUtil.filterContent(commentSql);
                    CompletableFuture<Void> future = this.executeAsync(commentSql);
                    futures.add(future);
                }
            });
            try {
                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get(3, TimeUnit.SECONDS);
            } catch (Exception e) {
                e.printStackTrace();
                LOGGER.error("模型:{}创建表注释异常，准备删除当前模型",modelJsonBean.getTitle());
                executeAsync("DROP TABLE " + prefix + modelJsonBean.getTableName());
                throw new BusinessException("模型异常，请重新拖拽模型导入");
            }
        }
    }

    @Override
    public boolean updateConfig(String modelId, ModelJsonBean modelJsonBean) {
        if (StringUtils.isEmpty(modelId) || modelJsonBean == null) {
            return false;
        }
        return this.updateConfig(modelId, modelJsonBean, "");
    }



    @Override
    public void updateModelField(ModelJsonBean modelJsonBean, ModelEntity modelEntity, String modelType) {
        // 更新业务信息
        Map json = new HashMap();
        json.put("html", modelJsonBean.getHtml());
        json.put("searchJson", modelJsonBean.getSearchJson());
        json.put("script", modelJsonBean.getScript());
        json.put("isWebSubmit", modelJsonBean.isWebSubmit());
        json.put("isWebCode", modelJsonBean.isWebCode());
        json.put("form", modelJsonBean.getForm());
        json.put("id", modelJsonBean.getId());

        //因为ModelAop会进行站群插件id拼接，保存模型的时候需要还原最原始的表名称，方便复制JSON模型使用
        if (BasicUtil.getWebsiteApp() != null) {
            if (StringUtils.isNotEmpty(modelJsonBean.getSql())) {
                json.put("sql", modelJsonBean.getSql().replace("_" + BasicUtil.getWebsiteApp().getId(), ""));
            }

            json.put("tableName", modelJsonBean.getTableName().replace("_" + BasicUtil.getWebsiteApp().getId(), ""));
        } else {
            if (StringUtils.isNotEmpty(modelJsonBean.getSql())) {
                json.put("sql", modelJsonBean.getSql());
            }
            json.put("tableName", modelJsonBean.getTableName());
        }
        if (modelJsonBean.getField() != null) {
            modelEntity.setModelField(modelJsonBean.getField());
        } else {
            modelEntity.setModelField("[]");
        }

        modelEntity.setModelName(modelJsonBean.getTitle());
        modelEntity.setModelType(modelType);
        modelEntity.setModelJson(JSONUtil.toJsonStr(json));
        modelEntity.setUpdateDate(new Date());
        //保存自定义模型实体
        this.updateById(modelEntity);
    }


    @Override
    public boolean updateConfig(String modelId, ModelJsonBean modelJsonBean, String modelType) {
        if (StringUtils.isEmpty(modelId) || modelJsonBean == null) {
            return false;
        }
        ModelEntity modelEntity = super.getById(modelId);
        if (ObjectUtil.isNull(modelEntity)) {
            return false;
        }

        //模型名称必须唯一，需要进行查询判断
        ModelEntity model = new ModelEntity();
        model.setModelName(modelJsonBean.getTitle());
        model.setModelCustomType(modelEntity.getModelCustomType());
        ModelEntity oldModel = super.getOne(new QueryWrapper<>(model));
        //判断表名是否存在
        if (ObjectUtil.isNotNull(oldModel) && !modelEntity.getId().equals(oldModel.getId())) {
            return false;
        }

        // 原始表名
        String oldTableName = modelEntity.getModelTableName();

        if (modelEntity.getModelCustomType().equalsIgnoreCase(ModelCustomTypeEnum.MODEL.getLabel())) {
            //在表名前面拼接前缀
            modelEntity.setModelTableName(("MDIY_MODEL_" + modelJsonBean.getTableName()).toUpperCase());
            if (!oldTableName.equals(modelEntity.getModelTableName())) {
                LOGGER.error("该模型新老表名不一致，老表名：{}，新表名：{}", oldTableName, modelEntity.getModelTableName());
                // TODO: 2023/10/25 更新模型 不允许表名修改
                throw new BusinessException(BundleUtil.getBaseString("err.error",
                        BundleUtil.getString(net.mingsoft.mdiy.constant.Const.RESOURCES, "table.name")));
            }
        }
        if (modelEntity.getModelCustomType().equalsIgnoreCase(ModelCustomTypeEnum.FORM.getLabel())) {
            //在表名前面拼接前缀
            modelEntity.setModelTableName(("MDIY_FORM_" + modelJsonBean.getTableName()).toUpperCase());
            if (StringUtils.isNotBlank(modelJsonBean.getTableName()) && !oldTableName.equalsIgnoreCase(modelEntity.getModelTableName())) {
                LOGGER.error("该模型新老表名不一致，老表名：{}，新表名：{}", oldTableName, modelEntity.getModelTableName());
                // TODO: 2023/10/25 更新模型 不允许表名修改
                throw new BusinessException(BundleUtil.getBaseString("err.error",
                        BundleUtil.getString(net.mingsoft.mdiy.constant.Const.RESOURCES, "table.name")));
            }
        }

        // 更新表结构
        updateTable(modelEntity.getModelField(), modelJsonBean.getField(), oldTableName);
        // 更新模型字段 field
        updateModelField(modelJsonBean, modelEntity, modelType);
        return true;
    }


    /**
     * 批量删除，并且删除表
     *
     * @param ids
     * @return
     */
    @Override
    public boolean delete(List<String> ids) {
        for (String id : ids) {
            ModelEntity modelEntity = super.getById(id);
            if (ObjectUtil.isNull(modelEntity)){
                throw new BusinessException(BundleUtil.getBaseString("err.error",BundleUtil.getString(Const.RESOURCES,"model.id")));
            }
            boolean flag = super.removeById(id);
            if (!flag) {
                LOG.debug("{}删除失败", modelEntity.getModelTableName());
                break;
            } else {
                try {
                    // 表名为空，跳出删表操作
                    if (StrUtil.isBlank(modelEntity.getModelTableName())) {
                        continue;
                    }
                    // 名称过滤
                    SqlInjectionUtil.checkStandardTableColumnName(modelEntity.getModelTableName());
                    this.executeAsync("DROP TABLE " + modelEntity.getModelTableName());
                } catch (Exception e) {
                    LOG.debug("{}表不存在", modelEntity.getModelTableName());
                    e.printStackTrace();
                }
            }
        }
        return true;
    }

    @Override
    public List<ModelEntity> query(ModelEntity modelEntity) {
        AppEntity websiteApp = BasicUtil.getWebsiteApp();
        // 1. 防止进入非站群管理查询，导致查询数据错误。
        if (websiteApp != null) {
            // 判断当前站点是否为主站点
            boolean isMasterApp = BasicUtil.isMasterApp(websiteApp.getId());
            // 放这里，防止查询是否为主站点为消耗分页资源
            BasicUtil.startPage();
            return modelDao.queryForSite(modelEntity, websiteApp.getId(), isMasterApp);
        }
        BasicUtil.startPage();
        return modelDao.query(modelEntity);
    }

    @Override
    public ModelEntity getByEntity(ModelEntity modelEntity) {
        // 1. 模型类型和模型名称不能为空，否则查询出来时是多个数据，会报错
        if (StringUtils.isBlank(modelEntity.getModelCustomType()) && StringUtils.isBlank(modelEntity.getModelName())) {
            return null;
        }
        AppEntity websiteApp = BasicUtil.getWebsiteApp();
        // 2. 判断是否在站群环境下，防止进入非站群管理查询，导致查询失败
        if (websiteApp != null) {
            // 判断当前站点是否为主站点
            boolean isMasterApp = BasicUtil.isMasterApp(websiteApp.getId());
            return modelDao.getByEntity(modelEntity, websiteApp.getId(), isMasterApp);
        }
        LambdaQueryWrapper<ModelEntity> wrapper = new LambdaQueryWrapper<>(modelEntity);
        return modelDao.selectOne(wrapper);
    }

    @Override
    public ModelEntity getById(Serializable id) {
        if (BasicUtil.isMasterApp()) {
            return modelDao.getEntityById(String.valueOf(id));
        }
        return modelDao.selectById(id);
    }

    @Override
    public ModelEntity getEntityById(String id) {
        return modelDao.getEntityById(id);
    }

    @Override
    public boolean updateById(ModelEntity modelEntity) {
        if (StringUtils.isBlank(modelEntity.getId())) {
            return false;
        }
        AppEntity websiteApp = BasicUtil.getWebsiteApp();
        // 1. 开启站群时，更新全局标签时会拼接appId条件,所以得忽略apppId
        if (websiteApp != null) {
            return SqlHelper.retBool(modelDao.updateEntityById(modelEntity));
        }
        return SqlHelper.retBool(modelDao.updateById(modelEntity));
    }

    private void updateTable(String oldStr, String newStr, String tableName) {
        List<Dict> oldList = JSONUtil.toList(oldStr, Dict.class);
        List<Dict> newList = JSONUtil.toList(newStr, Dict.class);
        StringBuffer stringBuffer = new StringBuffer();
        // 获取两个集合的差集
        Collection<Dict> disMap = CollUtil.disjunction(oldList, newList);
        if (CollUtil.isNotEmpty(disMap)) {
            // 旧字符串中删除和修改的集合
            Collection<Dict> oldIntersection = CollUtil.intersection(oldList, disMap);
            String alertTable = "";
            stringBuffer.append(alertTable);
            if (CollUtil.isNotEmpty(oldIntersection)) {
                // 预执行SQL
                String dropSql = StrUtil.format("ALTER TABLE {} ", tableName.toUpperCase()).concat("DROP COLUMN {};");
                // 先通过循环找到已经删除的字段，删除字段需要drop 操作
                List<String> dropList = oldIntersection.stream()
                        .map(dict -> {
                            // 旧字段在新字段里找，如果没有找到，说明该字段已经被删除，直接drop掉
                            List<Dict> collect = newList.stream()
                                    .filter(_dict -> _dict.getStr("field").equals(dict.getStr("field")))
                                    .collect(Collectors.toList());
                            // 找不到说明没找到，已经没有该字段啦
                            if (CollUtil.isEmpty(collect)) {
                                return StrUtil.format(dropSql, dict.getStr("field"));
                            }
                            return null;
                        }).collect(Collectors.toList());
                // 移除里面所有的null
                dropList.removeAll(Collections.singleton(null));
                if (CollUtil.isNotEmpty(dropList)) {
                    stringBuffer.append(CollUtil.join(dropList, ";"));
                }
            }
            // 新字符串中新增和修改的集合
            Collection<Dict> newIntersection = CollUtil.intersection(newList, disMap);
            if (CollUtil.isNotEmpty(newIntersection)) {
                SqlQueryWrapper sqlQueryWrapper = new SqlQueryWrapper();
                List<String> addList = newIntersection.stream().map(dict -> {
                    List<Dict> collect = oldList.stream()
                            .filter(_dict -> _dict.getStr("field").equals(dict.getStr("field")))
                            .collect(Collectors.toList());
                    // 对INT和BIGINT类型做规范 不能设置长度
                    if (StrUtil.equalsAnyIgnoreCase(dict.getStr("jdbcType"), "INT", "BIGINT")){
                        dict.set("length", "0");
                    }
                    // 不为空，说明它只更改了类型和名字
                    if (CollUtil.isEmpty(collect)) {
                        return sqlQueryWrapper.handleAddColumn(tableName.toUpperCase(), dict);
                    }
                    return sqlQueryWrapper.handleModifyColumn(tableName.toUpperCase(), dict);
                }).collect(Collectors.toList());
                if (CollUtil.isNotEmpty(addList)) {
                    stringBuffer.append(CollUtil.join(addList, ";"));
                }
            }
            LOG.debug("执行的SQL：{}", stringBuffer);

            String[] formSqls = stringBuffer.toString().split(";");
            List<CompletableFuture<Void>> futures = new ArrayList<>();
            for (String sql : formSqls) {
                //insert和delete语句不能执行
                if (StringUtils.isBlank(sql) || StrUtil.containsAnyIgnoreCase(sql, "SELECT ", "INSERT ", "DELETE ", "DELETE ")) {
                    continue;
                }
                CompletableFuture<Void> future = this.executeAsync(sql);
                futures.add(future);
            }
            try {
                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get(3, TimeUnit.SECONDS);
            } catch (Exception e) {
                e.printStackTrace();
                // TODO: 2024/9/25 这里防止原业务字段不存在或者字段被修改（直接修改数据库之类的）但是filed字段没有修改导致报错，统一捕获处理。
                throw new BusinessException("模型更新失败");
            }

        }


    }

}
