



package net.mingsoft.mdiy.action.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import net.mingsoft.base.entity.ResultData;
import net.mingsoft.base.util.SqlInjectionUtil;
import net.mingsoft.basic.action.BaseAction;
import net.mingsoft.basic.bean.EUListBean;
import net.mingsoft.basic.util.BasicUtil;
import net.mingsoft.mdiy.biz.IDictBiz;
import net.mingsoft.mdiy.entity.DictEntity;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 通用自定义字典
 * 创建日期：2017年11月8日<br/>
 * 历史修订：<br/>
 */
@Tag(name = "前端-自定义模块接口")
@Controller("webDictAction")
@RequestMapping("/mdiy/dict")
public class DictAction extends BaseAction{

	/**
	 * 注入字典表业务层
	 */
	@Autowired
	private IDictBiz dictBiz;

	/**
	 * 查询字典表列表
	 * @param dict 字典表实体
	 * <i>dict参数包含字段信息参考：</i><br/>
	 * dictId 编号<br/>
	 * dictAppId 应用编号<br/>
	 * dictValue 数据值<br/>
	 * dictLabel 标签名<br/>
	 * dictType 类型<br/>
	 * dictDescription 描述<br/>
	 * dictSort 排序（升序）<br/>
	 * createBy 创建者<br/>
	 * createDate 创建时间<br/>
	 * updateBy 更新者<br/>
	 * updateDate 更新时间<br/>
	 * dictRemarks 备注信息<br/>
	 * del 删除标记<br/>
	 * <dt><span class="strong">返回</span></dt><br/>
	 * <dd>[<br/>
	 * { <br/>
	 * dictId: 编号<br/>
	 * dictAppId: 应用编号<br/>
	 * dictValue: 数据值<br/>
	 * dictLabel: 标签名<br/>
	 * dictType: 类型<br/>
	 * dictDescription: 描述<br/>
	 * dictSort: 排序（升序）<br/>
	 * createBy: 创建者<br/>
	 * createDate: 创建时间<br/>
	 * updateBy: 更新者<br/>
	 * updateDate: 更新时间<br/>
	 * dictRemarks: 备注信息<br/>
	 * del: 删除标记<br/>
	 * }<br/>
	 * ]</dd><br/>
	 */
	@Operation(summary =  "查询字典表列表")
	@Parameters({
		@Parameter(name = "dictType", description = "类型", required =  false, in = ParameterIn.QUERY),
    	@Parameter(name = "dictValue", description = "数据值", required = false, in = ParameterIn.QUERY),
    	@Parameter(name = "dictLabel", description = "标签名", required =  false, in = ParameterIn.QUERY),
    	@Parameter(name = "dictSort", description = "排序（升序）", required =  false, in = ParameterIn.QUERY),
    	@Parameter(name = "isChild", description = "子业务关联", required =  false, in = ParameterIn.QUERY),
    })
	@GetMapping("/list")
	@ResponseBody
	public ResultData list(@ModelAttribute @Parameter(hidden = true) DictEntity dict, HttpServletResponse response, HttpServletRequest request) {
		// 检查SQL注入
		SqlInjectionUtil.filterContent(dict.getOrderBy());
		dict.setSqlWhere("");
		if(dict == null || StringUtils.isEmpty(dict.getDictType())){
			return ResultData.build().error(this.getResString("dict.type"));
		}
		BasicUtil.startPage(1,100,true);
		//增加条件字典启用状态，子业务查询启用的字典
		dict.setDictEnable(true);
		List dictList = dictBiz.query(dict);
		return ResultData.build().success(new EUListBean(dictList,(int)BasicUtil.endPage(dictList).getTotal()));
	}

	@Operation(summary =  "查询字典表列表,排除站点编号")
	@Parameters({
			@Parameter(name = "dictValue", description = "数据值", required = false, in = ParameterIn.QUERY),
			@Parameter(name = "dictLabel", description = "标签名", required =  false, in = ParameterIn.QUERY),
			@Parameter(name = "dictType", description = "类型", required =  false, in = ParameterIn.QUERY),
			@Parameter(name = "dictDescription", description = "描述", required =  false, in = ParameterIn.QUERY),
			@Parameter(name = "dictSort", description = "排序（升序）", required =  false, in = ParameterIn.QUERY),
			@Parameter(name = "isChild", description = "子业务关联", required =  false, in = ParameterIn.QUERY),
	})
	@GetMapping("/listExcludeApp")
	@ResponseBody
	public ResultData listExcludeApp(@ModelAttribute @Parameter(hidden = true) DictEntity dict, HttpServletResponse response, HttpServletRequest request) {
		// 检查SQL注入
		SqlInjectionUtil.filterContent(dict.getOrderBy());
		dict.setSqlWhere("");
		//增加条件字典启用状态，子业务查询启用的字典
		dict.setDictEnable(true);
		List dictList = dictBiz.queryExcludeApp(dict);
		return ResultData.build().success(dictList);
	}

}
