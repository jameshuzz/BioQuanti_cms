





package net.mingsoft.mdiy.action;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.json.JSONUtil;
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
import net.mingsoft.basic.annotation.LogAnn;
import net.mingsoft.basic.constant.e.BusinessTypeEnum;
import net.mingsoft.basic.util.BasicUtil;
import net.mingsoft.mdiy.biz.IConfigBiz;
import net.mingsoft.mdiy.biz.IModelBiz;
import net.mingsoft.mdiy.entity.ConfigEntity;
import net.mingsoft.mdiy.entity.ModelEntity;
import net.mingsoft.mdiy.util.ConfigUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

/**
 * 通用模型配置数据
 * 历史修订: 2022-1-27 get() update() 从使用configBiz改为configUtil
 */
@Tag(name = "后端-自定义模块接口")
@Controller
@RequestMapping("/${ms.manager.path}/mdiy/config/data")
public class ConfigDataAction extends BaseAction {

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
     * 自定义配置
     */
    @Hidden
    @GetMapping("/form")
    public String form(HttpServletResponse response, HttpServletRequest request, @Parameter(hidden = true) ModelMap model) {
        return "/mdiy/config/data/form";
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
    public ResultData get(HttpServletResponse response, HttpServletRequest request) {
        String modelId = BasicUtil.getString("modelId");
        if (StringUtils.isEmpty(modelId)) {
            return ResultData.build().error(getResString("err.empty",getResString("model.id")));
        }
        ModelEntity modelEntity = modelBiz.getById(modelId);
        if (modelEntity == null) {
            return ResultData.build().error(getResString("err.error", this.getResString("model.id")));
        }
        if(!hasPermissions("mdiy:configData:view","mdiy:configData:" + modelEntity.getId()+ ":view")){
            return ResultData.build().error("没有权限!");
        }

        ConfigEntity configEntity = configBiz.getOne(new LambdaQueryWrapper<ConfigEntity>().eq(ConfigEntity::getModelId, modelEntity.getId()));
        if (configEntity == null) {
            return ResultData.build().error(getResString("err.error", getResString("config.name")));
        }
        return ResultData.build().success(StringUtils.isBlank(configEntity.getConfigData())?null:JSONUtil.parseObj(configEntity.getConfigData()));
    }

    @Operation(summary =  "更新自定义配置")
    @LogAnn(title = "更新自定义配置", businessType = BusinessTypeEnum.UPDATE)
    @PostMapping("/update")
    @ResponseBody
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
        if(!hasPermissions("mdiy:configData:update","mdiy:configData:" + modelEntity.getId() + ":update")){
            return ResultData.build().error("没有权限!");
        }
        ConfigEntity configEntity = configBiz.getOne(new LambdaQueryWrapper<ConfigEntity>().eq(ConfigEntity::getModelId, modelEntity.getId()));
        if (configEntity == null) {
            return ResultData.build().error(getResString("err.empty", this.getResString("config.name")));
        }
        List<Map> modelFields = JSONUtil.toList(modelEntity.getModelField(), Map.class);

        //优化自定义配置保存时候，数据字段类型全部为字符串的问题，导致前端返回值控件也会提示类型错误
        modelFields.forEach(field-> {
            String model = MapUtil.getStr(field, "model");
            if (StringUtils.isBlank(model)) {
                return;
            }
            String value = MapUtil.getStr(map, model);
            if(StringUtils.isNotBlank(value)) {
                String javaType = MapUtil.getStr(field, "javaType");
                if ("Integer".equalsIgnoreCase(javaType)) {
                    // 判断是否为整数，防止Integer.parseInt()方法报错
                    if (NumberUtil.isInteger(value)) {
                        map.put(model, Integer.parseInt(value));
                    } else if (NumberUtil.isDouble(value)) {
                        map.put(model, Double.valueOf(value));
                    }
                }
                if ("Boolean".equalsIgnoreCase(javaType)) {
                    map.put(model, Boolean.parseBoolean(value));
                }
            }

        });

        configEntity.setConfigData(JSONUtil.toJsonStr(map));
        configBiz.updateById(configEntity);
        configBiz.updateCache();
        return ResultData.build().success(configEntity);
    }

    /**
     *  获取配置中的key指定value值
     * @param configName 配置名称
     * @param key 配置的key值
     * @param response
     * @param request
     * @return
     */
    @Operation(summary =  "获取自定义配置中的key指定value值接口")
    @Parameters({
            @Parameter(name = "configName", description = "配置名称", required = true, in = ParameterIn.QUERY),
            @Parameter(name = "key", description = "配置的key值", required = true, in = ParameterIn.QUERY),
    })
    @GetMapping("/getMap")
    @ResponseBody
    public ResultData getMap(String configName,String key, HttpServletResponse response, HttpServletRequest request){
        if(StringUtils.isBlank(key))  {
            return ResultData.build().success(ConfigUtil.getMap(configName));
        } else {
            return ResultData.build().success(ConfigUtil.getString(configName,key));
        }

    }

}
