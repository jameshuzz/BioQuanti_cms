



package net.mingsoft.mdiy.action;

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
import net.mingsoft.basic.util.StringUtil;
import net.mingsoft.mdiy.biz.IDictBiz;
import net.mingsoft.mdiy.entity.DictEntity;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * 字典表管理控制层,对每个单独的字典类型进行管理
 */
@Tag(name = "后端-自定义模块接口")
@Controller
@RequestMapping("/${ms.manager.path}/mdiy/dict/data")
public class DictDataAction extends BaseAction {

    /**
     * 注入字典表业务层
     */
    @Autowired
    private IDictBiz dictBiz;


    /**
     * 返回主界面index
     */
    @Hidden
    @GetMapping("/index")
    public String index(HttpServletResponse response, HttpServletRequest request) {
        return "/mdiy/dict/data/index";
    }

    /**
     * 返回编辑界面dict_form
     */
    @Hidden
    @GetMapping("/form")
    public String form(@ModelAttribute DictEntity dict, HttpServletResponse response, HttpServletRequest request, @Parameter(hidden = true) ModelMap model) {

        return "/mdiy/dict/data/form";
    }

    /**
     * 查询字典表列表
     */
    @Operation(summary =  "查询字典表列表接口")
    @Parameters({
            @Parameter(name = "dictType", description = "类型", required =  false, in = ParameterIn.QUERY),
            @Parameter(name = "dictLabel", description = "标签名", required =  false, in = ParameterIn.QUERY),
            @Parameter(name = "dictValue", description = "数据值", required =  false, in = ParameterIn.QUERY),
            @Parameter(name = "isChild", description = "子业务关联", required =  false, in = ParameterIn.QUERY),
    })
    @RequestMapping(value = "/list", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public ResultData list(@ModelAttribute @Parameter(hidden = true) DictEntity dict, HttpServletResponse response, HttpServletRequest request, @Parameter(hidden = true) ModelMap model) {
        if (StringUtils.isBlank(dict.getDictType())) {
            return ResultData.build().error("字典类型不能为空");
        }
        if (!hasPermissions("mdiy:dict:view", "mdiy:dictData:" + dict.getDictType() + ":settings")) {
            return ResultData.build().error("没有权限!");
        }
        BasicUtil.startPage();
        List dictList = dictBiz.query(dict);
        return ResultData.build().success(new EUListBean(dictList, (int) BasicUtil.endPage(dictList).getTotal()));
    }


    /**
     * 获取字典表
     */
    @Operation(summary =  "获取字典详情接口")
    @Parameter(name = "id", description = "字典编号", required =  true, in = ParameterIn.QUERY)
    @GetMapping("/get")
    @ResponseBody
    public ResultData get(@ModelAttribute @Parameter(hidden = true) DictEntity dict, HttpServletResponse response, HttpServletRequest request, @Parameter(hidden = true) ModelMap model) {
        if (StringUtils.isBlank(dict.getId())) {
            return ResultData.build().error(getResString("err.error", this.getResString("dict.id")));
        }
        DictEntity _dict = dictBiz.getById(dict.getId());
        if (StringUtils.isBlank(_dict.getDictType())) {
            return ResultData.build().error("字典类型不能为空");
        }
        if (!hasPermissions("mdiy:dict:view", "mdiy:dictData:" + _dict.getDictType() + ":settings")) {
            return ResultData.build().error("没有权限!");
        }
        return ResultData.build().success(_dict);
    }

    /**
     * 保存字典表实体
     */
    @Operation(summary =  "保存字典接口")
    @Parameters({
            @Parameter(name = "dictLabel", description = "标签名", required =  true, in = ParameterIn.QUERY),
            @Parameter(name = "dictType", description = "类型", required =  true, in = ParameterIn.QUERY),
            @Parameter(name = "dictValue", description = "数据值", required =  false, in = ParameterIn.QUERY),
            @Parameter(name = "dictDescription", description = "描述", required =  false, in = ParameterIn.QUERY),
            @Parameter(name = "dictSort", description = "排序（升序）", required =  false, in = ParameterIn.QUERY),
            @Parameter(name = "isChild", description = "子业务关联", required =  false, in = ParameterIn.QUERY),
            @Parameter(name = "dictRemarks", description = "备注信息", required =  false, in = ParameterIn.QUERY)
    })
    @LogAnn(title = "保存字典接口", businessType = BusinessTypeEnum.INSERT)
    @PostMapping("/save")
    @ResponseBody
    public ResultData save(@ModelAttribute @Parameter(hidden = true) DictEntity dict, HttpServletResponse response, HttpServletRequest request) {
        if (!hasPermissions("mdiy:dict:save", "mdiy:dictData:" + dict.getDictType() + ":settings")) {
            return ResultData.build().error("没有权限!");
        }

        // 类型不能为空
        if (StringUtils.isBlank(dict.getDictType())) {
            return ResultData.build().error(getResString("err.empty", this.getResString("dict.type")));
        }
        if (!StringUtil.checkLength(dict.getDictType() + "", 1, 100)) {
            return ResultData.build().error(getResString("err.length", this.getResString("dict.type"), "1", "100"));
        }
        // 字典名不能为空
        if (StringUtils.isBlank(dict.getDictLabel())) {
            return ResultData.build().error(getResString("err.empty", this.getResString("dict.label")));
        }
        if (!StringUtil.checkLength(dict.getDictLabel() + "", 1, 100)) {
            return ResultData.build().error(getResString("err.length", this.getResString("dict.label"), "1", "100"));
        }
        // 字典值不能为空
        if (StringUtils.isBlank(dict.getDictValue())) {
            return ResultData.build().error(getResString("err.empty", this.getResString("dict.value")));
        }

        if (!StringUtil.checkLength(dict.getDictValue() + "", 1, 100)) {
            return ResultData.build().error(getResString("err.length", this.getResString("dict.value"), "1", "100"));
        }
        if (!StringUtil.checkLength(dict.getDictDescription() + "", 0, 100)) {
            return ResultData.build().error(getResString("err.length", this.getResString("dict.description"), "0", "100"));
        }
        if (!StringUtil.checkLength(dict.getDictRemarks() + "", 0, 200)) {
            return ResultData.build().error(getResString("err.length", this.getResString("dict.remarks"), "0", "200"));
        }
        if (dict.getDictEnable()==null){
            return ResultData.build().error(getResString("err.empty",this.getResString("dict.enable")));
        }
        // type和lable不能为重复
        if (dictBiz.getByTypeAndLabelAndValue(dict.getDictType(), dict.getDictLabel(), null) != null) {
            return ResultData.build().error(getResString("diy.dict.type.and.label.repeat"));
        }
        // type和value不能为重复
        if (dictBiz.getByTypeAndLabelAndValue(dict.getDictType(), null, dict.getDictValue()) != null) {
            return ResultData.build().error(getResString("diy.dict.type.and.value.repeat"));
        }
        dictBiz.save(dict);
        // 刷新缓存
        dictBiz.updateCache();
        return ResultData.build().success();
    }

    /**
     * @param dicts 字典表实体
     *              <i>dict参数包含字段信息参考：</i><br/>
     *              id:id,id=1,2,3,4
     *              批量删除字典表
     *              <dt><span class="strong">返回</span></dt><br/>
     *              <dd>{code:"错误编码",<br/>
     *              result:"true｜false",<br/>
     *              resultMsg:"错误信息"<br/>
     *              }</dd>
     */
    @Operation(summary =  "批量删除字典")
    @LogAnn(title = "批量删除字典", businessType = BusinessTypeEnum.DELETE)
    @PostMapping("/delete")
    @ResponseBody
    public ResultData delete(@RequestBody List<DictEntity> dicts, HttpServletResponse response, HttpServletRequest request) {
        int[] ids = new int[dicts.size()];
        for (int i = 0; i < dicts.size(); i++) {
            ids[i] = Integer.parseInt(dicts.get(i).getId());
        }
        if (!hasPermissions("mdiy:dict:del", "mdiy:dictData:" + dicts.get(0).getDictType() + ":settings")) {
            return ResultData.build().error("没有权限!");
        }
        dictBiz.delete(ids);
        return ResultData.build().success();
    }

    /**
     * 更新字典表信息字典表
     */
    @Operation(summary =  "更新字典信息接口")
    @Parameters({
            @Parameter(name = "id", description = "字典编号", required =  true, in = ParameterIn.QUERY),
            @Parameter(name = "dictLabel", description = "标签名", required =  true, in = ParameterIn.QUERY),
            @Parameter(name = "dictType", description = "类型", required =  true, in = ParameterIn.QUERY),
            @Parameter(name = "dictValue", description = "数据值", required =  false, in = ParameterIn.QUERY),
            @Parameter(name = "dictDescription", description = "描述", required =  false, in = ParameterIn.QUERY),
            @Parameter(name = "dictSort", description = "排序（升序）", required =  false, in = ParameterIn.QUERY),
            @Parameter(name = "isChild", description = "子业务关联", required =  false, in = ParameterIn.QUERY),
            @Parameter(name = "dictRemarks", description = "备注信息", required =  false, in = ParameterIn.QUERY)
    })
    @LogAnn(title = "更新字典信息接口", businessType = BusinessTypeEnum.UPDATE)
    @PostMapping("/update")
    @ResponseBody
    public ResultData update(@ModelAttribute @Parameter(hidden = true) DictEntity dict, HttpServletResponse response,
                             HttpServletRequest request) {

        if (!hasPermissions("mdiy:dict:update", "mdiy:dictData:" + dict.getDictType() + ":settings")) {
            return ResultData.build().error("没有权限!");
        }

        // 类型不能为空
        if (StringUtils.isBlank(dict.getDictType())) {
            return ResultData.build().error(getResString("err.empty", this.getResString("dict.type")));
        }
        if (!StringUtil.checkLength(dict.getDictType() + "", 1, 100)) {
            return ResultData.build().error(getResString("err.length", this.getResString("dict.type"), "1", "100"));
        }
        // 字典名不能为空
        if (StringUtils.isBlank(dict.getDictLabel())) {
            return ResultData.build().error(getResString("err.empty", this.getResString("dict.label")));
        }
        if (!StringUtil.checkLength(dict.getDictLabel() + "", 1, 100)) {
            return ResultData.build().error(getResString("err.length", this.getResString("dict.label"), "1", "100"));
        }
        // 字典值不能为空
        if (StringUtils.isBlank(dict.getDictValue())) {
            return ResultData.build().error(getResString("err.empty", this.getResString("dict.value")));
        }

        if (!StringUtil.checkLength(dict.getDictValue() + "", 1, 100)) {
            return ResultData.build().error(getResString("err.length", this.getResString("dict.value"), "1", "100"));
        }
        if (!StringUtil.checkLength(dict.getDictDescription() + "", 0, 100)) {
            return ResultData.build().error(getResString("err.length", this.getResString("dict.description"), "0", "100"));
        }
        if (!StringUtil.checkLength(dict.getDictRemarks() + "", 0, 200)) {
            return ResultData.build().error(getResString("err.length", this.getResString("dict.remarks"), "0", "200"));
        }
        if (dict.getDictEnable()==null){
            return ResultData.build().error(getResString("err.empty",this.getResString("dict.enable")));
        }
        DictEntity dictEntity = dictBiz.getByTypeAndLabelAndValue(dict.getDictType(), dict.getDictLabel(), null);
        // type和lable不能为重复
        if (dictEntity != null && !dictEntity.getId().equals(dict.getId())) {
            return ResultData.build().error(getResString("diy.dict.type.and.label.repeat"));
        }
        // type和value不能为重复
        dictEntity = dictBiz.getByTypeAndLabelAndValue(dict.getDictType(), null, dict.getDictValue());
        if (dictEntity != null && !dictEntity.getId().equals(dict.getId())) {
            return ResultData.build().error(getResString("diy.dict.type.and.value.repeat"));
        }


        dictBiz.updateById(dict);
        dictBiz.updateCache();
        return ResultData.build().success();
    }

}
