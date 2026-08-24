





package net.mingsoft.mdiy.action;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.map.CaseInsensitiveMap;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.mingsoft.base.entity.ResultData;
import net.mingsoft.base.exception.BusinessException;
import net.mingsoft.basic.annotation.LogAnn;
import net.mingsoft.basic.constant.e.BusinessTypeEnum;
import net.mingsoft.basic.util.BasicUtil;
import net.mingsoft.mdiy.biz.IModelBiz;
import net.mingsoft.mdiy.biz.IModelDataBiz;
import net.mingsoft.mdiy.constant.e.ModelCustomTypeEnum;
import net.mingsoft.mdiy.entity.ModelEntity;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Map;

/**
 * 通用业务数据
 */
@Tag(name = "后端-自定义模块接口")
@Controller
@RequestMapping("/${ms.manager.path}/mdiy/form/data")
public class FormDataAction extends BaseAction {

    // 自定义业务参数错误的code标识，方便前端区分错误和异常
    private final static String PARAM_ERR = "PARAMERR";

    /**
     * 注入自定义配置业务层
     */
    @Autowired
    private IModelDataBiz modelDataBiz;

    @Autowired
    private IModelBiz modelBiz;

    /**
     * 扩展模型表单
     */
    @Hidden
    @GetMapping("/index")
    public String index(HttpServletResponse response, HttpServletRequest request, @Parameter(hidden = true) ModelMap model){
        String modelId = BasicUtil.getString("modelId");
        ModelEntity modelEntity = modelBiz.getOne(new LambdaQueryWrapper<ModelEntity>()
                .eq(ModelEntity::getId, modelId)
                .eq(ModelEntity::getModelCustomType, ModelCustomTypeEnum.FORM.getLabel()));
        if (modelEntity == null){
            throw new BusinessException(getResString("err.not.exist",getResString("model.id")));
        }
        return "/mdiy/form/data/index";
    }

    /**
     * 扩展模型表单
     */
    @Hidden
    @GetMapping("/form")
    public String form(HttpServletResponse response, HttpServletRequest request, @Parameter(hidden = true) ModelMap model){
        String businessForm = BasicUtil.getString("businessUrl");
        if (StringUtils.isNotBlank(businessForm)) {
            return businessForm;
        }
        return "/mdiy/form/data/form";
    }

    /**
     * 提供前端查询自定义表单提交数据
     *
     * @param request
     * @param response
     */
    @Operation(summary =  "提供后台查询自定义表单提交数据")
    @Parameters({
            @Parameter(name = "modelId", description = "模型编号", required =  true, in = ParameterIn.QUERY),
            @Parameter(name = "modelName", description = "模型名称", required =  false, in = ParameterIn.QUERY),
    })
    @RequestMapping(value = "/queryData", method = {RequestMethod.GET,RequestMethod.POST})
    @ResponseBody
    public ResultData queryData( HttpServletRequest request, HttpServletResponse response) {
        //获取表单id
        Map<String, Object> map = BasicUtil.assemblyRequestMap();
        String modelId = MapUtil.getStr(map, "modelId");
        if (StringUtils.isBlank(modelId)) {
            return ResultData.build().error(getResString("err.empty",getResString("model.id")));
        }
        LambdaQueryWrapper<ModelEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ModelEntity::getId,modelId).eq(ModelEntity::getModelCustomType,ModelCustomTypeEnum.FORM.getLabel());
        ModelEntity modelEntity = modelBiz.getOne(wrapper, false);
        if (ObjectUtil.isNull(modelEntity)) {
            return ResultData.build().error(getResString("err.error",getResString("model.id")));
        }

        if(!hasPermissions("mdiy:formData:view","mdiy:formData:" + modelEntity.getId() + ":view")){
            return ResultData.build().error("没有权限!");
        }
        // 默认排序
        map.putIfAbsent("order", "desc");
        map.putIfAbsent("orderBy", "id");
        return ResultData.build().success(modelDataBiz.queryDiyFormData(modelEntity.getId(), map));
    }

    /**
     * 提供前端查询自定义表单提交数据
     *
     * @param request
     * @param response
     */
    @Operation(summary =  "提供后台查询自定义表单提交数据")
    @Parameters({
            @Parameter(name = "modelId", description = "模型编号", required =  true, in = ParameterIn.QUERY),
//            @Parameter(name = "modelName", description = "模型名称", required =  false, in = ParameterIn.QUERY),
            @Parameter(name = "id", description = "主键编号", required =  true, in = ParameterIn.QUERY)
    })
    @GetMapping("/getData")
    @ResponseBody
    public ResultData getData( HttpServletRequest request, HttpServletResponse response) {
        //获取表单id
        String modelId = BasicUtil.getString("modelId");
//        String modelName = BasicUtil.getString("modelName");
        String id = BasicUtil.getString("id");
        if(StringUtils.isBlank(modelId)){
            return ResultData.build().error(getResString("err.empty",getResString("model.id")));
        }
        if(StringUtils.isBlank(id)){
            return ResultData.build().error(getResString("err.empty",getResString("id")));
        }
        LambdaQueryWrapper<ModelEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ModelEntity::getId,modelId);//.or().eq(ModelEntity::getModelName,modelName);
        wrapper.eq(ModelEntity::getModelCustomType, ModelCustomTypeEnum.FORM.getLabel());
        ModelEntity modelEntity = modelBiz.getOne(wrapper, false);
        if (ObjectUtil.isNull(modelEntity)) {
            return ResultData.build().error(getResString("err.error",getResString("model.id")));
        }
        Object object = modelDataBiz.getFormData(modelId, id);
        if (ObjectUtil.isNotNull(object) ) {
            return ResultData.build().success(object);
        }
        return ResultData.build().error();
    }


    /**
     * 保存自定义业务数据
     * 注意：在返回ResultData.build().error()时，请务必设置code错误码，区分业务错误和异常，方便前端根据code做出提示信息
     */
    @Operation(summary = "自定义业务数据保存")
    @Parameters({
            @Parameter(name = "modelName", description = "业务模型名称", required =  true, in = ParameterIn.QUERY),
            @Parameter(name = "modelId", description = "业务模型Id", required =  false, in = ParameterIn.QUERY),
    })
    @LogAnn(title = "新增自定义业务数据",businessType= BusinessTypeEnum.INSERT)
    @PostMapping("save")
    @ResponseBody
    public ResultData save(HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> map = BasicUtil.assemblyRequestMap();
        CaseInsensitiveMap<String,Object> caseIgnoreMap = new CaseInsensitiveMap<>(map);
        String modelName = BasicUtil.getString("modelName");
        String modelId = BasicUtil.getString("modelId");
        if(StringUtils.isBlank(modelName) && StringUtils.isBlank(modelId)){
            return ResultData.build().error(getResString("err.empty",getResString("model.id"))).code(PARAM_ERR);
        }

        LambdaQueryWrapper<ModelEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StringUtils.isNotEmpty(modelName),ModelEntity::getModelName, modelName)
                .eq(StringUtils.isNotEmpty(modelId), ModelEntity::getId, modelId)
                .eq(ModelEntity::getModelCustomType, ModelCustomTypeEnum.FORM.getLabel());
        ModelEntity modelEntity = modelBiz.getOne(wrapper, true);
        if (modelEntity == null) {
            return ResultData.build().error(getResString("err.not.exist", this.getResString("model.name"))).code(PARAM_ERR);
        }
        if(!hasPermissions("mdiy:formData:save","mdiy:formData:" + modelEntity.getId() + ":save")){
            return ResultData.build().error("没有权限!").code(PARAM_ERR);
        }
        if (modelDataBiz.saveDiyFormData(modelEntity.getId(),caseIgnoreMap)) {
            return ResultData.build().success();
        }else {
            return ResultData.build().error(getResString("err.error",getResString("model.id"))).code(PARAM_ERR);
        }
    }


    /**
     * 更新自定义业务数据
     * 注意：在返回ResultData.build().error()时，请务必设置code错误码，区分业务错误和异常，方便前端根据code做出提示信息
     */
    @Operation(summary = "更新自定义业务数据")
    @Parameter(name = "modelId", description = "模型编号", required =  true, in = ParameterIn.QUERY)
    @LogAnn(title = "更新自定义业务数据",businessType= BusinessTypeEnum.UPDATE)
    @PostMapping("update")
    @ResponseBody
    public ResultData update(HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> map = BasicUtil.assemblyRequestMap();
        CaseInsensitiveMap<String,Object> caseIgnoreMap = new CaseInsensitiveMap<>(map);

        String modelId = caseIgnoreMap.get("modelId").toString();
        if(StringUtils.isBlank(modelId)){
            return ResultData.build().error(getResString("err.empty",getResString("model.id"))).code(PARAM_ERR);
        }
        ModelEntity modelEntity = modelBiz.getById(modelId);
        if(!hasPermissions("mdiy:formData:update","mdiy:formData:" + modelEntity.getId() + ":update")){
            return ResultData.build().error("没有权限!").code(PARAM_ERR);
        }
        if (modelDataBiz.updateDiyFormData(modelEntity,caseIgnoreMap)) {
            return ResultData.build().success();
        }else {
            return ResultData.build().error(getResString("err.error",getResString("model.id"))).code(PARAM_ERR);
        }
    }

    /**
     * 复制数据
     */
    @Operation(summary =  "复制指定自定义业务数据")
    @Parameters({
            @Parameter(name = "modelId", description = "模型编号", required =  true, in = ParameterIn.QUERY),
            @Parameter(name = "id", description = "主键编号", required =  true, in = ParameterIn.QUERY)
    })
    @LogAnn(title = "复制指定自定义业务数据",businessType= BusinessTypeEnum.INSERT)
    @PostMapping("/copy")
    @ResponseBody
    public ResultData copy(@RequestParam(value = "id", required = true) String id, @RequestParam(value = "modelId", required = true) String modelId) {
        // 先判断是否有复制权限
        if (!hasPermissions("mdiy:formData:copy","mdiy:formData:" + modelId + ":copy")) {
            return ResultData.build().error("insufficient.permissions").code(PARAM_ERR);
        }
        if (StrUtil.isBlank(id) || StrUtil.isBlank(modelId)) {
            return ResultData.build().error(getResString("err.empty", this.getResString("id")));
        }

        LambdaQueryWrapper<ModelEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ModelEntity::getId, modelId)
                .eq(ModelEntity::getModelCustomType, ModelCustomTypeEnum.FORM.getLabel());
        ModelEntity modelEntity = modelBiz.getOne(wrapper, true);
        // 判断模型是否存在
        if (modelEntity == null) {
            return ResultData.build().error(getResString("err.not.exist", this.getResString("model.id")));
        }

        // 通过模型id和数据id获取业务数据
        Object formData = modelDataBiz.getFormData(modelId, id);

        if (ObjectUtil.isEmpty(formData)) {
            return ResultData.build().error(getResString("err.error", this.getResString("id")));
        }

        // 转换小写，以便aop能检测唯一性校验
        CaseInsensitiveMap camelCaseMap = new CaseInsensitiveMap(BeanUtil.toBean(formData, Map.class));

        // 把id移除
        camelCaseMap.remove("id");

        // 保存业务数据
        if (modelDataBiz.saveDiyFormData(modelId, camelCaseMap)) {
            return ResultData.build().success();
        }

        return ResultData.build().error(getResString("err.error", this.getResString("id")));
    }

    @Operation(summary =  "批量删除自定义业务数据接口")
    @LogAnn(title = "批量删除自定义业务数据接口",businessType= BusinessTypeEnum.DELETE)
    @PostMapping("delete")
    @ResponseBody
    public ResultData delete(@RequestParam("modelId") String modelId, HttpServletResponse response, HttpServletRequest request) {
        String ids = BasicUtil.getString("ids");

        if (StringUtils.isBlank(ids)) {
            return ResultData.build().error(getResString("err.empty",getResString("id")));
        }
        if (StringUtils.isBlank(modelId)) {
            return ResultData.build().error(getResString("err.empty",getResString("model.id")));
        }
        ModelEntity modelEntity = modelBiz.getById(modelId);

        if(!hasPermissions("mdiy:formData:del","mdiy:formData:" + modelEntity.getId() + ":del")){
            return ResultData.build().error("没有权限!");
        }

        String [] _ids = ids.split(",");
        modelDataBiz.deleteDiyFormData(modelId, Arrays.asList(_ids));
        return ResultData.build().success();
    }

}
