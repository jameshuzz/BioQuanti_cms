





package net.mingsoft.mdiy.action;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.map.CaseInsensitiveMap;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.stuxuhai.jpinyin.PinyinException;
import com.github.stuxuhai.jpinyin.PinyinHelper;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.mingsoft.base.entity.BaseEntity;
import net.mingsoft.base.entity.ResultData;
import net.mingsoft.base.util.SqlInjectionUtil;
import net.mingsoft.basic.annotation.LogAnn;
import net.mingsoft.basic.bean.EUListBean;
import net.mingsoft.basic.constant.e.BusinessTypeEnum;
import net.mingsoft.basic.util.BasicUtil;
import net.mingsoft.mdiy.bean.ModelJsonBean;
import net.mingsoft.mdiy.biz.IModelBiz;
import net.mingsoft.mdiy.biz.IModelDataBiz;
import net.mingsoft.mdiy.constant.e.ModelCustomTypeEnum;
import net.mingsoft.mdiy.entity.ModelEntity;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 通用模型
 */
@Tag(name = "后端-自定义模块接口")
@Controller("coMdiyModelAction")
@RequestMapping("/${ms.manager.path}/mdiy/model")
public class ModelAction extends BaseAction {

    /**
     * 注入自定义模型业务层
     */
    @Autowired
    private IModelBiz modelBiz;

    @Autowired
    private IModelDataBiz modelDataBiz;

    /**
     * 寻找文件正则
     */
    private Pattern filePattern = Pattern.compile("(src|href)=\"(upload/.*?(png|jpg|gif))");
    /**
     * 返回主界面index
     */
    @Hidden
    @GetMapping("/index")
    public String index(HttpServletResponse response,HttpServletRequest request){
        return "/mdiy/model/index";
    }

    /**
     * 返回编辑界面model_form
     */
    @Hidden
    @GetMapping("/form")
    public String form(@ModelAttribute ModelEntity modelEntity,HttpServletResponse response,HttpServletRequest request,ModelMap modelMap){
        if(modelEntity.getId()!=null){
            BaseEntity _modelEntity = modelBiz.getById(modelEntity.getId());
            modelMap.addAttribute("modelEntity",_modelEntity);
        }
        return "/mdiy/model/form";
    }

    /**
     * 返回主界面index
     */
    @Hidden
    @GetMapping("/design")
    public String design(HttpServletResponse response,HttpServletRequest request){
        return "/mdiy/model/design";
    }


    /**
     * 查询自定义模型列表
     * @param model 自定义模型实体
     * <i>model参数包含字段信息参考：</i><br/>
     * modelName 模型名称<br/>
     * modelTableName 模型表名<br/>
     * appId 应用编号<br/>
     * modelJson json<br/>
     * id 编号<br/>
     * <dt><span class="strong">返回</span></dt><br/>
     * <dd>[<br/>
     * { <br/>
     * modelName: 模型名称<br/>
     * modelTableName: 模型表名<br/>
     * appId: 应用编号<br/>
     * modelJson: json<br/>
     * id: 编号<br/>
     * }<br/>
     * ]</dd><br/>
     */
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
            @Parameter(name = "del", description = "删除标记", required = false, in = ParameterIn.QUERY),
            @Parameter(name = "id", description = "编号", required = false, in = ParameterIn.QUERY),
    })
    @GetMapping("/list")
    @ResponseBody
    public ResultData list(@ModelAttribute @Parameter(hidden = true) ModelEntity modelEntity, HttpServletResponse response, HttpServletRequest request, @Parameter(hidden = true) ModelMap model, BindingResult result) {
        modelEntity.setModelCustomType(ModelCustomTypeEnum.MODEL.getLabel());
        List<ModelEntity> modelList = modelBiz.query(modelEntity);
        return ResultData.build().success(new EUListBean(modelList,(int)BasicUtil.endPage(modelList).getTotal()));
    }

    /**
     * 返回自定义模型数据
     */
    @Operation(summary =  "查询自定义模型数据接口")
    @GetMapping("/data")
    @ResponseBody
    public ResultData data(String modelId,String linkId,HttpServletResponse response,HttpServletRequest request,ModelMap modelMap){
        if(StringUtils.isBlank(modelId)){
            return ResultData.build().error(getResString("err.empty",getResString("model.id")));
        }
        if (StringUtils.isBlank(linkId)) {
            return ResultData.build().error(getResString("err.empty",getResString("model.link.id")));
        }
        ModelEntity model =modelBiz.getOne(new LambdaQueryWrapper<ModelEntity>().eq(ModelEntity::getId, modelId)
                .eq(ModelEntity::getModelCustomType, ModelCustomTypeEnum.MODEL.getLabel()));
        if (ObjectUtil.isNull(model)) {
            return ResultData.build().error(getResString("err.error",getResString("model.id")));
        }
        return ResultData.build().success(modelDataBiz.getModelDataByLinkId(model,linkId));
    }


    @Operation(summary =  "保存模型接口")
    @LogAnn(title = "保存模型",businessType= BusinessTypeEnum.INSERT)
    @PostMapping("/data/save")
    @ResponseBody
    public ResultData save(String linkId,String modelId,HttpServletResponse response,HttpServletRequest request,ModelMap modelMap){
        ModelEntity model = modelBiz.getById(modelId);
        Map modelData = modelDataBiz.getModelDataByLinkId(model, linkId);
        if (MapUtil.isNotEmpty(modelData)) {
            // 如果当前link_id存在数据
            return ResultData.build().error(linkId);
        }
        Map<String, Object> requestMap = BasicUtil.assemblyRequestMap();
        // 转换成小写
        CaseInsensitiveMap<String,Object> map = new CaseInsensitiveMap<>(requestMap);
        modelDataBiz.spliceInsertSql(model, map);
        return ResultData.build().success(linkId);
    }

    @Operation(summary =  "更新模型接口")
    @LogAnn(title = "更新模型",businessType= BusinessTypeEnum.UPDATE)
    @PostMapping("/data/update")
    @ResponseBody
    public ResultData update(String linkId, String modelId,HttpServletResponse response,HttpServletRequest request,ModelMap modelMap){
        ModelEntity model = modelBiz.getById(modelId);
        Map<String, Object> requestMap = BasicUtil.assemblyRequestMap();
        // 转换成小写
        CaseInsensitiveMap<String,Object> map = new CaseInsensitiveMap<>(requestMap);
        Map modelData = modelDataBiz.getModelDataByLinkId(model, linkId);
        //如果没有数据，又可能是后面使用了自定义模型
        if(MapUtil.isEmpty(modelData)) {
            modelDataBiz.spliceInsertSql(model, map);
        } else {
            //更新
            modelDataBiz.spliceUpdateSql(model, map);
        }
        return ResultData.build().success(linkId);
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
    public ResultData get(@Parameter(hidden = true) ModelEntity modelEntity, HttpServletResponse response, HttpServletRequest request){
        //自定义模型是可以根据id或名称获取自定义模型，目前id主要是栏目自与文章栏目切换的时候使用id查询
        if(StringUtils.isEmpty(modelEntity.getModelName()) && StringUtils.isEmpty(modelEntity.getId())){
            return ResultData.build().error(this.getResString("err.error",this.getResString("model.name")));
        }
        modelEntity.setModelCustomType(ModelCustomTypeEnum.MODEL.getLabel());
        ModelEntity model = modelBiz.getByEntity(modelEntity);
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

    @Operation(summary =  "新增自定义模型接口")
    @Parameters({
            @Parameter(name = "modelJson", description = "json", required = true, in = ParameterIn.QUERY),
    })
    @LogAnn(title = "新增自定义模型接口",businessType= BusinessTypeEnum.INSERT)
    @PostMapping("/save")
    @ResponseBody
    @RequiresPermissions("mdiy:model:importJson")
    public ResultData saveModel(@ModelAttribute @Parameter(hidden = true) ModelEntity modelEntity, HttpServletResponse response, HttpServletRequest request) {
        if (StringUtils.isBlank(modelEntity.getModelName())) {
            return ResultData.build().error(this.getResString("err.empty",this.getResString("model.name")));
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
        // 检查是否有重复模型名称或者重复表名
        String tableName = "MDIY_" + ModelCustomTypeEnum.MODEL.getLabel().toUpperCase() + "_" + modelTableName;
        LambdaQueryWrapper<ModelEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ModelEntity::getModelCustomType, ModelCustomTypeEnum.MODEL.getLabel())
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
        model.setModelCustomType(ModelCustomTypeEnum.MODEL.getLabel());
        model.setModelType(modelEntity.getModelType());

        modelBiz.save(model);

        return ResultData.build().success(model);

    }

    @Operation(summary =  "导入自定义模型接口")
    @Parameters({
            @Parameter(name = "modelJson", description = "json", required = true, in = ParameterIn.QUERY),
    })
    @LogAnn(title = "导入自定义模型",businessType= BusinessTypeEnum.INSERT)
    @PostMapping("/importJson")
    @ResponseBody
    @RequiresPermissions("mdiy:model:importJson")
    public ResultData importJson(@ModelAttribute @Parameter(hidden = true) ModelEntity modelEntity, HttpServletResponse response, HttpServletRequest request,BindingResult result) {


        //验证json的值是否合法
        if(StringUtils.isBlank(modelEntity.getModelJson())){
            return ResultData.build().error(getResString("err.empty", this.getResString("model.json")));
        }

        modelEntity.setModelCustomType(ModelCustomTypeEnum.MODEL.getLabel());

        ModelJsonBean modelJsonBean = new ModelJsonBean();
        try{
            modelJsonBean = JSONUtil.toBean(modelEntity.getModelJson(), ModelJsonBean.class);
        }catch (Exception e){
            return ResultData.build().error(getResString("err.error", this.getResString("model.json")));
        }

        // 保存导入的json模型
        if(modelBiz.importModel(ModelCustomTypeEnum.MODEL.getLabel(), modelJsonBean,modelEntity.getModelType())){
            return ResultData.build().success();
        }else {
            return ResultData.build().error(getResString("err.exist", this.getResString("table.name")));
        }

    }

    @Operation(summary =  "更新导入自定义模型")
    @Parameters({
            @Parameter(name = "modelJson", description = "json", required = true, in = ParameterIn.QUERY),
    })
    @LogAnn(title = "更新自定义模型",businessType= BusinessTypeEnum.INSERT)
    @PostMapping("/updateJson")
    @ResponseBody
    @RequiresPermissions("mdiy:model:update")
    public ResultData updateJson(@ModelAttribute @Parameter(hidden = true) ModelEntity modelEntity, HttpServletResponse response, HttpServletRequest request, BindingResult result) {
        //验证json的值是否合法
        if(StringUtils.isBlank(modelEntity.getModelJson())){
            return ResultData.build().error(getResString("err.empty", this.getResString("model.json")));
        }
        if(StringUtils.isBlank(modelEntity.getId())){
            return ResultData.build().error(getResString("err.empty", this.getResString("id")));
        }
        ModelJsonBean modelJsonBean = null;
        try{
            modelJsonBean = JSONUtil.toBean(modelEntity.getModelJson(), ModelJsonBean.class);
        }catch (Exception e){
            return ResultData.build().error(getResString("err.error", this.getResString("model.json")));
        }
        // 保存导入的json模型
        if(modelBiz.updateConfig(modelEntity.getId(), modelJsonBean,modelEntity.getModelType())){
            return ResultData.build().success();
        }else {
            return ResultData.build().error(getResString("err.exist", this.getResString("table.name")));
        }
    }


    /**
     * @param models 自定义模型实体
     * <i>model参数包含字段信息参考：</i><br/>
     * id:多个id直接用逗号隔开,例如id=1,2,3,4
     * 批量删除自定义模型
     *            <dt><span class="strong">返回</span></dt><br/>
     *            <dd>{code:"错误编码",<br/>
     *            result:"true｜false",<br/>
     *            resultMsg:"错误信息"<br/>
     *            }</dd>
     */
    @Operation(summary =  "批量删除自定义模型列表接口")
    @LogAnn(title = "批量删除自定义模型",businessType= BusinessTypeEnum.DELETE)
    @PostMapping("/delete")
    @ResponseBody
    @RequiresPermissions("mdiy:model:del")
    public ResultData delete(@RequestBody List<ModelEntity> models,HttpServletResponse response, HttpServletRequest request) {
        List<String> ids = models.stream().map(ModelEntity::getId).collect(Collectors.toList());
        modelBiz.delete(ids);
        return ResultData.build().success();
    }



}
