



package net.mingsoft.mdiy.action;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
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
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 字典表管理控制层
 *
 * @version 版本号：1<br/>
 * 创建日期：2017-8-12 14:22:36<br/>
 * 历史修订：2022-1-25 新增 importJson() 方法
 */
@Tag(name = "后端-自定义模块接口")
@Controller
@RequestMapping("/${ms.manager.path}/mdiy/dict")
public class DictAction extends BaseAction {

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
        return "/mdiy/dict/index";
    }

	/**
	 * 返回编辑界面dict_form
	 */
	@Hidden
	@GetMapping("/form")
	public String form(@ModelAttribute DictEntity dict, HttpServletResponse response, HttpServletRequest request, @Parameter(hidden = true) ModelMap model) {

		return "/mdiy/dict/form";
	}

    @Operation(summary =  "导入自定义字典")
    @LogAnn(title = "导入自定义字典", businessType = BusinessTypeEnum.INSERT)
    @PostMapping("/importJson")
    @ResponseBody
    @RequiresPermissions("mdiy:dict:importJson")
    public ResultData importJson(@RequestBody List<DictEntity> dictEntities, HttpServletResponse response, HttpServletRequest request, BindingResult result) {
        //验证list的值是否合法
        if (CollUtil.isEmpty(dictEntities)) {
            return ResultData.build().error("json格式不匹配");
        }
        List<DictEntity> list = dictBiz.list();
        List<DictEntity> dictEntityList = dictEntities.stream()
                .filter(dictEntity -> !list.contains(dictEntity))
                .map(dictEntity -> {
                    dictEntity.setId(null);
                    return dictEntity;
                }).collect(Collectors.toList());

        if(CollUtil.isNotEmpty(dictEntityList)){
            dictBiz.saveBatch(dictEntityList, dictEntityList.size());
        }
        return ResultData.build().success();
    }


    /**
     * 查询字典表列表
     *
     * @param dict 字典表实体
     *             <i>dict参数包含字段信息参考：</i><br/>
     *             dictValue 数据值<br/>
     *             dictLabel 标签名<br/>
     *             dictType 类型<br/>
     *             dictDescription 描述<br/>
     *             dictSort 排序（升序）<br/>
     *             createBy 创建者<br/>
     *             createDate 创建时间<br/>
     *             updateBy 更新者<br/>
     *             updateDate 更新时间<br/>
     *             dictRemarks 备注信息<br/>
     *             del 删除标记<br/>
     *             <dt><span class="strong">返回</span></dt><br/>
     *             <dd>[<br/>
     *             { <br/>
     *             dictValue: 数据值<br/>
     *             dictLabel: 标签名<br/>
     *             dictType: 类型<br/>
     *             dictDescription: 描述<br/>
     *             dictSort: 排序（升序）<br/>
     *             createBy: 创建者<br/>
     *             createDate: 创建时间<br/>
     *             updateBy: 更新者<br/>
     *             updateDate: 更新时间<br/>
     *             dictRemarks: 备注信息<br/>
     *             del: 删除标记<br/>
     *             }<br/>
     *             ]</dd><br/>
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
        BasicUtil.startPage();
        //新增dictEnable字典状态字段，子业务默认只查询启用状态的字典数据
        //自定义字典管理页面查询全部
        //为了减少代码管理页面传入dictEnable参数来判断是否查询全部
        if (dict.getDictEnable() == null) {
            dict.setDictEnable(true);
        } else {
            dict.setDictEnable(null);
        }
        List dictList = dictBiz.query(dict);
        return ResultData.build().success(new EUListBean(dictList, (int) BasicUtil.endPage(dictList).getTotal()));
    }

    @Operation(summary =  "根据子业务类型获取所有字典类型")
    @Parameters({
            @Parameter(name = "isChild", description = "子业务关联", required =  false, in = ParameterIn.QUERY),
    })
    @GetMapping("/dictType")
    @ResponseBody
    public ResultData dictType(@ModelAttribute @Parameter(hidden = true) DictEntity dict, HttpServletResponse response, HttpServletRequest request, @Parameter(hidden = true) ModelMap model) {
        if (!hasPermissions("mdiy:dict:view", "mdiy:dictData:" + dict.getDictType() + ":settings")) {
            return ResultData.build().error("没有权限!");
        }
        BasicUtil.startPage();
        QueryWrapper<DictEntity> dictEntityQueryWrapper = new QueryWrapper<>();
        //使用min()，获取每个分类最小时间，按分类最早创建时间排序
        // sql适配， 规范group by的使用
        dictEntityQueryWrapper.select("dict_type,min(CREATE_DATE) as min_create_time");
        dictEntityQueryWrapper.groupBy("dict_type");
        dictEntityQueryWrapper.orderByDesc("min_create_time");
        List<DictEntity> dictList = dictBiz.list(dictEntityQueryWrapper);
        return ResultData.build().success(new EUListBean(dictList, (int) BasicUtil.endPage(dictList).getTotal()));
    }

    @Operation(summary =  "根据字典类型获取字典，可支持多个类型用英文逗号隔开")
    @Parameters({
            @Parameter(name = "dictType", description = "字典类型", required =  true, in = ParameterIn.QUERY),
            @Parameter(name = "isChild", description = "子业务关联", required =  false, in = ParameterIn.QUERY),
    })
    @GetMapping("/dictList")
    @ResponseBody
    public ResultData dictList(@ModelAttribute @Parameter(hidden = true) DictEntity dict, HttpServletResponse response, HttpServletRequest request, @Parameter(hidden = true) ModelMap model) {
        if (StringUtils.isEmpty(dict.getDictType())) {
            return ResultData.build().error(getResString("err.error", this.getResString("dict.type")));
        }
        String[] types = dict.getDictType().split(",");
        DictEntity _dict = new DictEntity();
        _dict.setIsChild(dict.getIsChild());
        List list = new ArrayList();
        for (String type : types) {
            _dict.setDictType(type);
            list.add(dictBiz.query(_dict));
        }
        return ResultData.build().success(list);
    }

    /**
     * 获取字典表
     *
     * @param dict 字典表实体
     *             <i>dict参数包含字段信息参考：</i><br/>
     *             dictValue 数据值<br/>
     *             dictLabel 标签名<br/>
     *             dictType 类型<br/>
     *             dictDescription 描述<br/>
     *             dictSort 排序（升序）<br/>
     *             createBy 创建者<br/>
     *             createDate 创建时间<br/>
     *             updateBy 更新者<br/>
     *             updateDate 更新时间<br/>
     *             dictRemarks 备注信息<br/>
     *             del 删除标记<br/>
     *             <dt><span class="strong">返回</span></dt><br/>
     *             <dd>{ <br/>
     *             dictValue: 数据值<br/>
     *             dictLabel: 标签名<br/>
     *             dictType: 类型<br/>
     *             dictDescription: 描述<br/>
     *             dictSort: 排序（升序）<br/>
     *             createBy: 创建者<br/>
     *             createDate: 创建时间<br/>
     *             updateBy: 更新者<br/>
     *             updateDate: 更新时间<br/>
     *             dictRemarks: 备注信息<br/>
     *             del: 删除标记<br/>
     *             }</dd><br/>
     */
    @Operation(summary =  "获取字典详情接口")
    @Parameter(name = "id", description = "字典编号", required =  true, in = ParameterIn.QUERY)
    @GetMapping("/get")
    @ResponseBody
    @RequiresPermissions("mdiy:dict:view")
    public ResultData get(@ModelAttribute @Parameter(hidden = true) DictEntity dict, HttpServletResponse response, HttpServletRequest request, @Parameter(hidden = true) ModelMap model) {
        if (StringUtils.isBlank(dict.getId())) {
            return ResultData.build().error(getResString("err.error", this.getResString("dict.id")));
        }
        DictEntity _dict = dictBiz.getById(dict.getId());
        return ResultData.build().success(_dict);
    }

    /**
     * 保存字典表实体
     *
     * @param dict 字典表实体
     *             <i>dict参数包含字段信息参考：</i><br/>
     *             dictValue 数据值<br/>
     *             dictLabel 标签名<br/>
     *             dictType 类型<br/>
     *             dictDescription 描述<br/>
     *             dictSort 排序（升序）<br/>
     *             createBy 创建者<br/>
     *             createDate 创建时间<br/>
     *             updateBy 更新者<br/>
     *             updateDate 更新时间<br/>
     *             dictRemarks 备注信息<br/>
     *             del 删除标记<br/>
     *             <dt><span class="strong">返回</span></dt><br/>
     *             <dd>{ <br/>
     *             dictValue: 数据值<br/>
     *             dictLabel: 标签名<br/>
     *             dictType: 类型<br/>
     *             dictDescription: 描述<br/>
     *             dictSort: 排序（升序）<br/>
     *             createBy: 创建者<br/>
     *             createDate: 创建时间<br/>
     *             updateBy: 更新者<br/>
     *             updateDate: 更新时间<br/>
     *             dictRemarks: 备注信息<br/>
     *             del: 删除标记<br/>
     *             }</dd><br/>
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
    @RequiresPermissions("mdiy:dict:save")
    public ResultData save(@ModelAttribute @Parameter(hidden = true) DictEntity dict, HttpServletResponse response, HttpServletRequest request) {
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
    @RequiresPermissions("mdiy:dict:del")
    public ResultData delete(@RequestBody List<DictEntity> dicts, HttpServletResponse response, HttpServletRequest request) {
        int[] ids = new int[dicts.size()];
        for (int i = 0; i < dicts.size(); i++) {
            ids[i] = Integer.parseInt(dicts.get(i).getId());
        }
        dictBiz.delete(ids);
        return ResultData.build().success();
    }

    /**
     * 更新字典表信息字典表
     *
     * @param dict 字典表实体
     *             <i>dict参数包含字段信息参考：</i><br/>
     *             dictValue 数据值<br/>
     *             dictLabel 标签名<br/>
     *             dictType 类型<br/>
     *             dictDescription 描述<br/>
     *             dictSort 排序（升序）<br/>
     *             createBy 创建者<br/>
     *             createDate 创建时间<br/>
     *             updateBy 更新者<br/>
     *             updateDate 更新时间<br/>
     *             dictRemarks 备注信息<br/>
     *             del 删除标记<br/>
     *             <dt><span class="strong">返回</span></dt><br/>
     *             <dd>{ <br/>
     *             dictValue: 数据值<br/>
     *             dictLabel: 标签名<br/>
     *             dictType: 类型<br/>
     *             dictDescription: 描述<br/>
     *             dictSort: 排序（升序）<br/>
     *             createBy: 创建者<br/>
     *             createDate: 创建时间<br/>
     *             updateBy: 更新者<br/>
     *             updateDate: 更新时间<br/>
     *             dictRemarks: 备注信息<br/>
     *             del: 删除标记<br/>
     *             }</dd><br/>
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
    @RequiresPermissions("mdiy:dict:update")
    public ResultData update(@ModelAttribute @Parameter(hidden = true) DictEntity dict, HttpServletResponse response,
                             HttpServletRequest request) {
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


    @Operation(summary =  "刷新字典缓存接口")
    @PostMapping("/updateCache")
    @RequiresPermissions("mdiy:dict:update")
    @ResponseBody
    public ResultData updateCache(HttpServletResponse response, HttpServletRequest request, @Parameter(hidden = true) ModelMap model) {
        dictBiz.updateCache();
        return ResultData.build().success();
    }

}
