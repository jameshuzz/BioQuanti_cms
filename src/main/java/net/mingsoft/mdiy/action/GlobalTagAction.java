





package net.mingsoft.mdiy.action;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import net.mingsoft.base.entity.ResultData;
import net.mingsoft.basic.annotation.LogAnn;
import net.mingsoft.basic.constant.e.BusinessTypeEnum;
import net.mingsoft.basic.util.BasicUtil;
import net.mingsoft.mdiy.bean.ModelJsonBean;
import net.mingsoft.mdiy.biz.IConfigBiz;
import net.mingsoft.mdiy.biz.IModelBiz;
import net.mingsoft.mdiy.biz.ITagBiz;
import net.mingsoft.mdiy.constant.e.ConfigTypeEnum;
import net.mingsoft.mdiy.constant.e.ModelCustomTypeEnum;
import net.mingsoft.mdiy.entity.ConfigEntity;
import net.mingsoft.mdiy.entity.ModelEntity;
import net.mingsoft.mdiy.entity.TagEntity;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

@Tag(name = "后端-自定义全局标签数据模块接口")
@Controller
@RequestMapping("/${ms.manager.path}/mdiy/tag/globalTag")
public class GlobalTagAction extends BaseAction {

    /**
     * 注入自定义配置业务层
     */
    @Autowired
    private IConfigBiz configBiz;

    /**
     * 注入自定义模型业务层
     */
    @Autowired
    private IModelBiz modelBiz;

    /**
     * 注入标签业务层
     */
    @Autowired
    private ITagBiz tagBiz;



    /**
     * 全局自定义配置
     */
    @Hidden
    @GetMapping("/config")
    @RequiresPermissions("mdiy:tag:config")
    public String config(HttpServletResponse response,HttpServletRequest request,@Parameter(hidden = true) ModelMap model){
        return "/mdiy/tag/global-tag/config";
    }

    /**
     * 配置数据获取
     *
     * @param response
     * @param request
     * @return
     */
    @Operation(summary =  "配置数据获取接口")
    @GetMapping("/get")
    @ResponseBody
    @RequiresPermissions("mdiy:tag:view")
    public ResultData get(HttpServletResponse response, HttpServletRequest request) {
        String modelId = BasicUtil.getString("modelId");
        if (StringUtils.isEmpty(modelId)) {
            return ResultData.build().error(getResString("err.empty",getResString("model.id")));
        }
        ModelEntity modelEntity = modelBiz.getById(modelId);
        if (modelEntity == null) {
            return ResultData.build().error(getResString("err.error", this.getResString("model.id")));
        }
        ConfigEntity configEntity = configBiz.getOne(new LambdaQueryWrapper<ConfigEntity>().eq(ConfigEntity::getModelId, modelEntity.getId()), true);
        if (configEntity == null) {
            return ResultData.build().error(getResString("err.error", getResString("config.name")));
        }
        return ResultData.build().success(JSONUtil.parseObj(configEntity.getConfigData()));
    }

    @Operation(summary =  "更新自定义全局配置数据")
    @LogAnn(title = "更新自定义全局配置数据", businessType = BusinessTypeEnum.UPDATE)
    @PostMapping("/update")
    @ResponseBody
    @RequiresPermissions("mdiy:tag:update")
    public ResultData update(HttpServletResponse response, HttpServletRequest request) {
        Map<String, Object> map = BasicUtil.assemblyRequestMap();
        String modelId = map.get("modelId").toString();
        if (StringUtils.isEmpty(modelId)) {
            return ResultData.build().error(getResString("err.empty",getResString("model.id")));
        }
        ModelEntity modelEntity = modelBiz.getById(modelId);
        if (modelEntity == null) {
            return ResultData.build().error(getResString("err.error", this.getResString("model.id")));
        }
        ConfigEntity configEntity = configBiz.getOne(new LambdaQueryWrapper<ConfigEntity>().eq(ConfigEntity::getModelId, modelEntity.getId()), true);
        if (configEntity == null) {
            return ResultData.build().error(getResString("err.empty", this.getResString("config.name")));
        }
        List<Map> modelFields = JSONUtil.toList(modelEntity.getModelField(), Map.class);


        //优化自定义配置保存时候，数据字段类型全部为字符串的问题，导致前端返回值控件也会提示类型错误
        modelFields.forEach(field-> {
            if(map.get(field.get("model"))!=null) {
                if(field.get("javaType").toString().equalsIgnoreCase("Integer")) {
                    map.put(field.get("model").toString(),Integer.parseInt(map.get(field.get("model")).toString()));
                }
                if(field.get("javaType").toString().equalsIgnoreCase("Boolean")) {
                    map.put(field.get("model").toString(),Boolean.parseBoolean(map.get(field.get("model")).toString()));
                }
            }

        });


        configEntity.setConfigData(JSONUtil.toJsonStr(map));
        configBiz.updateById(configEntity);
        configBiz.updateCache();
        return ResultData.build().success(configEntity);
    }

    /**
     * 通过此处获取模型数据，防止与其他模型类型冲突
     * 根据模型类型区分
     * @param response
     * @param request
     * @return
     */
    @Operation(summary =  "查询自定义模型接口")
    @Parameters({
            @Parameter(name = "modelName", description = "模型名称", required = false, in = ParameterIn.QUERY),
            @Parameter(name = "modelTableName", description = "模型表名", required = false, in = ParameterIn.QUERY),
            @Parameter(name = "appId", description = "应用编号", required = false, in = ParameterIn.QUERY),
            @Parameter(name = "modelJson", description = "json", required = false, in = ParameterIn.QUERY),
            @Parameter(name = "createBy", description = "创建人", required = false, in = ParameterIn.QUERY),
            @Parameter(name = "createDate", description = "创建时间", required = false, in = ParameterIn.QUERY),
            @Parameter(name = "updateBy", description = "修改人", required = false, in = ParameterIn.QUERY),
            @Parameter(name = "updateDate", description = "修改时间", required = false, in = ParameterIn.QUERY),
    })
    @GetMapping("/getModel")
    @ResponseBody
    public ResultData getModel(@Parameter(hidden = true) ModelEntity modelEntity, HttpServletResponse response, HttpServletRequest request){
        //自定义模型是可以根据id或名称获取自定义模型
        if(StringUtils.isEmpty(modelEntity.getModelName()) && StringUtils.isEmpty(modelEntity.getId())){
            return ResultData.build().error(this.getResString("err.error",this.getResString("model.name")));
        }
        // 获取全局标签
        modelEntity.setModelCustomType(ModelCustomTypeEnum.TAG.getLabel());
        ModelEntity model = modelBiz.getOne(new QueryWrapper<>(modelEntity));
        // 兼容旧模型
        try {
            JSONObject jsonObject = JSONUtil.parseObj(model.getModelJson());
            // 检查并处理 modelJson 中的 searchJson 字段
            if (jsonObject.containsKey("searchJson")) {
                String searchStr = jsonObject.getStr("searchJson");
                try {
                    // 验证 searchJson 是否为合法的 JSON 数组格式
                    JSONUtil.parseArray(searchStr);
                }catch (Exception e){
                    // 若不是合法的 JSON 数组，则将其设置为空数组字符串，防止后续解析错误
                    jsonObject.set("searchJson", "[]");
                    model.setModelJson(jsonObject.toString());
                    LOG.info("模型:{}——类型：{}中 searchJson 属性值非标准JSON格式，已替换为空数组",model.getModelName(),model.getModelCustomType());
                }
            }
        } catch (Exception e) {
            LOG.error("{}", e.getMessage());
        }
        return ResultData.build().success(model);
    }


    @Operation(summary =  "导入自定义模型")
    @Parameters({
            @Parameter(name = "modelJson", description = "json", required = true, in = ParameterIn.QUERY),
    })
    @LogAnn(title = "导入",businessType= BusinessTypeEnum.INSERT)
    @PostMapping("/importJson")
    @ResponseBody
    @RequiresPermissions("mdiy:tag:save")
    public ResultData importJson(@ModelAttribute @Parameter(hidden = true) ModelEntity modelEntity, HttpServletResponse response, HttpServletRequest request, BindingResult result) {
        String tagId = BasicUtil.getString("tagId");
        if (StringUtils.isBlank(tagId)) {
            return ResultData.build().error(getResString("err.empty", this.getResString("id")));
        }
        TagEntity tagEntity = tagBiz.getById(tagId);
        if (tagEntity == null) {
            return ResultData.build().error(getResString("err.error", this.getResString("id")));
        }
        //验证json的值是否合法
        if(StringUtils.isBlank(modelEntity.getModelJson())){
            return ResultData.build().error(getResString("err.empty", this.getResString("model.json")));
        }
        ModelJsonBean modelJsonBean = new ModelJsonBean();
        try{
            modelJsonBean = JSONUtil.toBean(modelEntity.getModelJson(), ModelJsonBean.class);
        }catch (Exception e){
            return ResultData.build().error(getResString("err.error", this.getResString("model.json")));
        }
        // 保存导入的json模型
        if(configBiz.importConfig(ConfigTypeEnum.TAG.getType(), modelJsonBean)){
            LambdaQueryWrapper<ModelEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(ModelEntity::getModelName, modelJsonBean.getTitle())
                    .eq(ModelEntity::getModelCustomType, ModelCustomTypeEnum.TAG.getLabel());
            // TODO: 2025/1/7 如果在站群环境下，通过这里导入的，基本都是站点标签，会自动拼接appId，不会查询空的情况
            modelEntity = modelBiz.getOne(queryWrapper);
            // 保存后赋值id给modelId
            tagEntity.setModelId(modelEntity.getId());
            tagBiz.updateById(tagEntity);
            // 刷新缓存，不然modelId为空
            tagBiz.updateCache();
            return ResultData.build().success(configBiz.getOne(new LambdaQueryWrapper<ConfigEntity>().eq(ConfigEntity::getModelId, modelEntity.getId())));
        }else {
            return ResultData.build().error(getResString("err.exist", this.getResString("table.name")));
        }
    }

    @Operation(summary =  "更新导入自定义模型")
    @Parameters({
            @Parameter(name = "modelJson", description = "json", required = true, in = ParameterIn.QUERY),
    })
    @LogAnn(title = "导入",businessType= BusinessTypeEnum.INSERT)
    @PostMapping("/updateJson")
    @ResponseBody
    @RequiresPermissions("mdiy:tag:update")
    public ResultData updateJson(@ModelAttribute @Parameter(hidden = true) ModelEntity modelEntity, HttpServletResponse response, HttpServletRequest request, BindingResult result) {
        //验证json的值是否合法
        if(StringUtils.isBlank(modelEntity.getModelJson())){
            return ResultData.build().error(getResString("err.empty", this.getResString("model.json")));
        }
        if(StringUtils.isBlank(modelEntity.getId())){
            return ResultData.build().error(getResString("err.empty", this.getResString("id")));
        }
        ModelJsonBean modelJsonBean = new ModelJsonBean();
        try{
            modelJsonBean = JSONUtil.toBean(modelEntity.getModelJson(), ModelJsonBean.class);
        }catch (Exception e){
            return ResultData.build().error(getResString("err.error", this.getResString("model.json")));
        }
        // 保存导入的json模型
        if(configBiz.updateConfig(modelEntity.getId(), modelJsonBean)){
            LambdaQueryWrapper<ConfigEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(ConfigEntity::getConfigName, modelJsonBean.getTitle())
                    .eq(ConfigEntity::getConfigType, ModelCustomTypeEnum.CONFIG.getLabel());
            return ResultData.build().success(configBiz.getOne(queryWrapper));
        }else {
            return ResultData.build().error(getResString("err.exist", this.getResString("table.name")));
        }
    }


}
