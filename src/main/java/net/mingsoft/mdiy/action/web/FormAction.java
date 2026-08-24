





package net.mingsoft.mdiy.action.web;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import net.mingsoft.base.entity.ResultData;
import net.mingsoft.mdiy.action.BaseAction;
import net.mingsoft.mdiy.biz.IModelBiz;
import net.mingsoft.mdiy.constant.e.ModelCustomTypeEnum;
import net.mingsoft.mdiy.entity.ModelEntity;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * 通用模型
 */
@Tag(name = "前端-自定义模块接口")
@Controller("webFormAction")
@RequestMapping("/mdiy/form")
public class FormAction extends BaseAction {

    /**
     * 注入自定义模型业务层
     */
    @Autowired
    private IModelBiz modelBiz;

    /**
     * 根据模型名称获取渲染表单
     *
     * @param response
     * @param request
     * @return
     */
    @Operation(summary =  "根据模型名称获取渲染表单")
    @Parameters({
        @Parameter(name = "modelName", description = "模型名称", required =  true, in = ParameterIn.QUERY),
    })
    @GetMapping("/get")
    @ResponseBody
    public ResultData get(String modelName, HttpServletResponse response, HttpServletRequest request) {
        if (StringUtils.isBlank(modelName)) {
            return ResultData.build().error(getResString("err.empty",getResString("model.name")));
        }
        LambdaQueryWrapper<ModelEntity> lqw  = new LambdaQueryWrapper<>();
        lqw.eq(ModelEntity::getModelCustomType,ModelCustomTypeEnum.FORM.getLabel())
                        .eq(ModelEntity::getModelName,modelName);
        ModelEntity model = modelBiz.getOne(lqw);
        if(ObjectUtil.isNull(model)) {
            return ResultData.build().error(getResString("err.error",getResString("model.name")));
        }
        //判断是否允许外部提交
        if(Boolean.parseBoolean(JSONUtil.toBean(model.getModelJson(), Map.class).get("isWebSubmit").toString())) {
            return ResultData.build().success(model);
        }
        return ResultData.build().error("此业务不允许外部提交");
    }

}
