



package net.mingsoft.mdiy.action;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.stuxuhai.jpinyin.PinyinException;
import com.github.stuxuhai.jpinyin.PinyinHelper;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import net.mingsoft.base.entity.ResultData;
import net.mingsoft.basic.annotation.LogAnn;
import net.mingsoft.basic.bean.EUListBean;
import net.mingsoft.basic.constant.e.BusinessTypeEnum;
import net.mingsoft.basic.util.BasicUtil;
import net.mingsoft.mdiy.bean.ModelJsonBean;
import net.mingsoft.mdiy.biz.IModelBiz;
import net.mingsoft.mdiy.constant.e.ModelCustomTypeEnum;
import net.mingsoft.mdiy.entity.ModelEntity;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 通用模型
 */
@Tag(name = "后端-自定义模块接口")
@Controller
@RequestMapping("/${ms.manager.path}/mdiy/form")
public class FormAction extends BaseAction {

    /**
     * 注入自定义配置业务层
     */
    @Autowired
    private IModelBiz modelBiz;

    /**
     * 主页
     * @param response
     * @param request
     * @return
     */
    @Hidden
    @GetMapping("/index")
    public String index(HttpServletResponse response, HttpServletRequest request){
        return "/mdiy/form/index";
    }


    @Operation(summary =  "查询自定义模型列表接口")
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
    @GetMapping("/list")
    @RequiresPermissions("mdiy:form:view")
    @ResponseBody
    public ResultData list(@ModelAttribute @Parameter(hidden = true) ModelEntity modelEntity, HttpServletResponse response, HttpServletRequest request) {
        modelEntity.setModelCustomType(ModelCustomTypeEnum.FORM.getLabel());
        BasicUtil.startPage();
        List modelList = modelBiz.list(new LambdaQueryWrapper<>(modelEntity).orderByDesc(ModelEntity::getId));
        return ResultData.build().success(new EUListBean(modelList,(int)BasicUtil.endPage(modelList).getTotal()));
    }

    /**
     * 通用渲染表单
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
    @GetMapping("/get")
    @ResponseBody
    public ResultData get(ModelEntity modelEntity, HttpServletResponse response, HttpServletRequest request){
        if(StringUtils.isBlank(modelEntity.getModelName()) && StringUtils.isBlank(modelEntity.getId())){
            return ResultData.build().error(this.getResString("err.error",this.getResString("model.name")+" or "+this.getResString("model.id")));
        }

        modelEntity.setModelCustomType(ModelCustomTypeEnum.FORM.getLabel());
        ModelEntity model = modelBiz.getOne(new QueryWrapper<>(modelEntity));
        if(model == null){
            return ResultData.build().error(this.getResString("err.not.exist",this.getResString("model.name")+" or "+this.getResString("model.id")));
        }
        if(!hasPermissions("mdiy:form:view","mdiy:formData:" + model.getId() + ":view")){
            return ResultData.build().error("没有权限!");
        }
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

    @Operation(summary =  "新增自定义业务接口")
    @LogAnn(title = "新增自定义业务接口",businessType= BusinessTypeEnum.INSERT)
    @PostMapping("/save")
    @ResponseBody
    @RequiresPermissions("mdiy:model:importJson")
    public ResultData save(@ModelAttribute @Parameter(hidden = true) ModelEntity modelEntity, HttpServletResponse response, HttpServletRequest request) {
        if (StringUtils.isBlank(modelEntity.getModelName())) {
            return ResultData.build().error(this.getResString("err.empty",this.getResString("model.name")));
        }

        // 验证模型名称的值是否合法
        if (!ReUtil.isMatch("^([\\u4e00-\\u9fa5a-zA-Z])([\\u4e00-\\u9fa5a-zA-Z0-9])*$", modelEntity.getModelName())) {
            return ResultData.build().error(this.getResString("err.error",this.getResString("model.name")));
        }

        // 因为代码生成器默认是大写 这里必须也设置成大写  业务名称首拼音+4个随机大写字母
        String modelTableName = "";
        try {
            modelTableName = PinyinHelper.getShortPinyin(modelEntity.getModelName()).toUpperCase()
                    + RandomUtil.randomString(RandomUtil.BASE_CHAR, 4).toUpperCase();
        } catch (PinyinException e) {
            LOG.debug("{}转拼音失败", modelEntity.getModelName());
            e.printStackTrace();
        }

        Map<String, Object> modelJson = new HashMap<>();
        modelJson.put("tableName", modelTableName);
        modelJson.put("id", 0);
        // 本地新增时由于数据库差异不创建表，由用户从代码生成器保存后，根据回调sql创建表，只有第一次有这个值
        modelJson.put("initSql", true);
        // 设置是否允许外部提交
        modelJson.put("isWebSubmit", BasicUtil.getBoolean("isWebSubmit", false));
        modelJson.put("isWebCode", BasicUtil.getBoolean("isWebCode", false));
        // 检查是否有重复模型名称或者重复表名
        String tableName = "MDIY_" + ModelCustomTypeEnum.FORM.getLabel().toUpperCase() + "_" + modelTableName;
        LambdaQueryWrapper<ModelEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ModelEntity::getModelCustomType, ModelCustomTypeEnum.FORM.getLabel())
                .and(w -> w.eq(ModelEntity::getModelName, modelEntity.getModelName()).or()
                        .eq(ModelEntity::getModelTableName, tableName));

        List<ModelEntity> modelEntities = modelBiz.list(wrapper);
        if (CollectionUtil.isNotEmpty(modelEntities)) {
            return ResultData.build().error("模型名称或模型表名重复");
        }

        ModelEntity model = new ModelEntity();
        // 只接受外部模型名称以及模型表名
        model.setModelName(modelEntity.getModelName());
        model.setModelTableName(tableName);
        // 默认雪花ID
        model.setModelIdType(0);
        model.setModelJson(JSONUtil.toJsonStr(modelJson));
        model.setModelField("[]");
        model.setModelCustomType(ModelCustomTypeEnum.FORM.getLabel());

        modelBiz.save(model);

        return ResultData.build().success(model);

    }


    @Operation(summary =  "导入自定义模型")
    @Parameters({
            @Parameter(name = "modelJson", description = "json", required = true, in = ParameterIn.QUERY),
    })
    @LogAnn(title = "导入",businessType= BusinessTypeEnum.INSERT)
    @PostMapping("/importJson")
    @ResponseBody
    @RequiresPermissions("mdiy:form:importJson")
    public ResultData importJson(@ModelAttribute @Parameter(hidden = true) ModelEntity modelEntity, HttpServletResponse response, HttpServletRequest request, BindingResult result) {
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
        if(modelBiz.importConfig(ModelCustomTypeEnum.FORM.getLabel(), modelJsonBean)){
            return ResultData.build().success();
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
    @RequiresPermissions("mdiy:form:update")
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
        if(modelBiz.updateConfig(modelEntity.getId(), modelJsonBean)){
            return ResultData.build().success();
        }else {
            return ResultData.build().error(getResString("err.exist", this.getResString("table.name")));
        }
    }


    @Operation(summary =  "批量删除自定义模型列表接口")
    @LogAnn(title = "批量删除自定义模型列表接口",businessType= BusinessTypeEnum.DELETE)
    @PostMapping("/delete")
    @ResponseBody
    @RequiresPermissions("mdiy:form:del")
    public ResultData delete(@RequestBody List<ModelEntity> models, HttpServletResponse response, HttpServletRequest request) {
        List<String> ids = models.stream().map(p -> p.getId()).collect(Collectors.toList());
        if (modelBiz.delete(ids)) {
            return ResultData.build().success();
        }else {
            return ResultData.build().error(getResString("err.error",getResString("id")));
        }

    }

}
