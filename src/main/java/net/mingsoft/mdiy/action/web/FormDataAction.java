



package net.mingsoft.mdiy.action.web;

import cn.hutool.core.map.CaseInsensitiveMap;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.mingsoft.base.entity.ResultData;
import net.mingsoft.basic.util.BasicUtil;
import net.mingsoft.mdiy.action.BaseAction;
import net.mingsoft.mdiy.biz.IModelBiz;
import net.mingsoft.mdiy.biz.IModelDataBiz;
import net.mingsoft.mdiy.constant.e.ModelCustomTypeEnum;
import net.mingsoft.mdiy.entity.ModelEntity;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 通用业务数据
 */
@Tag(name = "前端-自定义模块接口")
@Controller("webFormDataAction")
@RequestMapping("/mdiy/form/data")
public class FormDataAction extends BaseAction {

    /**
     * 注入自定义配置业务层
     */
    @Autowired
    private IModelDataBiz modelDataBiz;

    /**
     * 注入自定义模型
     */
    @Autowired
    private IModelBiz modelBiz;


    @Operation(summary = "前端自定义业务数据保存")
    @Parameters({
            @Parameter(name = "modelName", description = "业务模型名称", required =  true, in = ParameterIn.QUERY),
            @Parameter(name = "modelId", description = "业务模型Id", required =  false, in = ParameterIn.QUERY),
            @Parameter(name = "rand_code", description = "验证码", required =  false, in = ParameterIn.QUERY),
    })
    @PostMapping("save")
    @ResponseBody
    public ResultData save(HttpServletRequest request, HttpServletResponse response) {
        // 做modelId和modelName兼容
        String modelName = BasicUtil.getString("modelName");
        String modelId = BasicUtil.getString("modelId");
        if(StringUtils.isEmpty(modelName) && StringUtils.isEmpty(modelId)){
            return ResultData.build().error(getResString("err.empty",getResString("model.name")));
        }
        LOG.debug("保存表单");
        // 根据自定义模型Id和自定义模型名称查询
        LambdaQueryWrapper<ModelEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StringUtils.isNotEmpty(modelName),ModelEntity::getModelName, modelName)
                .eq(StringUtils.isNotEmpty(modelId), ModelEntity::getId, modelId)
                .eq(ModelEntity::getModelCustomType, ModelCustomTypeEnum.FORM.getLabel());
        ModelEntity model = modelBiz.getOne(wrapper, true);
        if (model == null) {
            return ResultData.build().error(getResString("err.not.exist", this.getResString("model.name")));
        }
        //判断是否允许外部提交
        if (!Boolean.parseBoolean(JSONUtil.toBean(model.getModelJson(), Map.class).get("isWebSubmit").toString())) {
             return ResultData.build().error(getResString("err.error", this.getResString("model.name")));
        }
        // 判断验证码是否校验, isWebCode如果为空，默认要验证(以防之前的没有isWebCode,做一个兼容)
        if (ObjectUtil.isNull(JSONUtil.toBean(model.getModelJson(), Map.class).get("isWebCode")) || Boolean.parseBoolean(JSONUtil.toBean(model.getModelJson(), Map.class).get("isWebCode").toString())) {
            //验证码
            if (!checkRandCode()) {
                LOG.debug("验证码错误");
                return ResultData.build().error(getResString("err.error", this.getResString("rand.code")));
            }
        }
        Map<String, Object> map = BasicUtil.assemblyRequestMap();
        map = new CaseInsensitiveMap<>(map);
        if (modelDataBiz.saveDiyFormData(model.getId(), map)) {
            return ResultData.build().success();
        } else {
            return ResultData.build().error(getResString("fail", this.getResString("save")));
        }

    }

    /**
     * 提供前端查询自定义表单提交数据
     *
     * @param modelName 业务名称
     * @param request
     * @param response
     */
    @Operation(summary =  "提供前端查询自定义表单提交数据")
    @Parameter(name = "modelName", description = "业务名称", required =  true, in = ParameterIn.QUERY)
    @GetMapping("list")
    @ResponseBody
    public ResultData list(@RequestParam("modelName")String modelName, HttpServletRequest request, HttpServletResponse response) {
        if (StringUtils.isBlank(modelName)) {
            return ResultData.build().error(getResString("err.empty",getResString("model.name")));
        }
        // 根据业务名称查询当前业务是否存在
        LambdaQueryWrapper<ModelEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ModelEntity::getModelCustomType, ModelCustomTypeEnum.FORM.getLabel())
                .eq(ModelEntity::getModelName, modelName);
        ModelEntity model = modelBiz.getOne(wrapper);
        if(ObjectUtil.isNull(model)){
            return ResultData.build().error(getResString("err.error", this.getResString("model.name")));
        }
        //判断是否允许外部提交
        if (!Boolean.parseBoolean(JSONUtil.toBean(model.getModelJson(), Map.class).get("isWebSubmit").toString())) {
            return ResultData.build().error("此业务不允许外部提交");
        }
        return ResultData.build().success(modelDataBiz.queryDiyFormData(model.getId(), BasicUtil.assemblyRequestMap()));
    }


}
