







package net.mingsoft.basic.action.web;

import cn.hutool.core.map.MapUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import net.mingsoft.base.entity.ResultData;
import net.mingsoft.basic.bean.CityBean;
import net.mingsoft.basic.biz.ICityBiz;
import net.mingsoft.basic.entity.CityEntity;
import net.mingsoft.basic.util.BasicUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * 省市县镇村数据管理控制层
 * @version
 * 版本号：100<br/>
 * 创建日期：2017-7-27 14:47:29<br/>
 * 历史修订：<br/>
 */
@Tag(name = "前端-基础接口")
@Controller("webCityAction")
@RequestMapping("/basic/city")
public class CityAction extends net.mingsoft.basic.action.BaseAction{

	/**
	 * 注入省市县镇村数据业务层
	 */
	@Autowired
	private ICityBiz cityBiz;

	/**
	 * 根据上一级的id查询下一级城市数据列表集合，没有参数默认查询省集合
	 * 例 basic/city/list.do?provinceId=36
	 * @param city
	 * @return
	 */
	@Operation(summary =  "根据id查询下一级城市数据列表集合")
	@Parameters({
			@Parameter(name = "provinceId", description = "省／直辖市／自治区级id", required =  false, in = ParameterIn.QUERY),
			@Parameter(name = "cityId", description = "市级id", required =  false, in = ParameterIn.QUERY),
			@Parameter(name = "countyId", description = "县／区级id", required =  false, in = ParameterIn.QUERY),
			@Parameter(name = "townId", description = "街道／镇级id", required =  false, in = ParameterIn.QUERY),
	})
	@GetMapping("/list")
	@ResponseBody
	public ResultData list(@Parameter(hidden = true) @ModelAttribute CityEntity city){
		Map<String, Object> stringObjectMap = BasicUtil.assemblyRequestMap();
		LambdaQueryWrapper<CityEntity> wrapper = new LambdaQueryWrapper<>(city);
		List<CityEntity> cityList;
		// 如果外部没有传递参数 默认只加载第一级
		if (MapUtil.isEmpty(stringObjectMap)){

			cityList = cityBiz.queryProvinceAndName(city);

			return ResultData.build().success(cityList);
		}else {
			// 根据外部传递的参数 指定查询的字段列和分组字段列 必须一致
			if (StringUtils.isNotBlank(MapUtil.getStr(stringObjectMap, "provinceId", null))){
				wrapper.select(CityEntity::getCityId,CityEntity::getCityName);
				wrapper.groupBy(CityEntity::getCityId,CityEntity::getCityName);
			}else if (StringUtils.isNotBlank(MapUtil.getStr(stringObjectMap, "cityId", null))){
				wrapper.select(CityEntity::getCountyId,CityEntity::getCountyName);
				wrapper.groupBy(CityEntity::getCountyId,CityEntity::getCountyName);
			}else if (StringUtils.isNotBlank(MapUtil.getStr(stringObjectMap, "countyId",null))){
				wrapper.select(CityEntity::getTownId,CityEntity::getTownName);
				wrapper.groupBy(CityEntity::getTownId,CityEntity::getTownName);
			}else if (StringUtils.isNotBlank(MapUtil.getStr(stringObjectMap, "townId", null))){
				wrapper.select(CityEntity::getVillageId,CityEntity::getVillageName);
				wrapper.groupBy(CityEntity::getVillageId,CityEntity::getVillageName);
			}
		}

		cityList = cityBiz.list(wrapper);
		return ResultData.build().success(cityList);
	}


	@Operation(summary =  "获取省市县镇村数据")
	@Parameter(name = "id", description = "城市主键编号", required =  true, in = ParameterIn.QUERY)
	@GetMapping("/get")
	@ResponseBody
	public ResultData get(@ModelAttribute @Parameter(hidden = true) CityEntity city, HttpServletResponse response, HttpServletRequest request, @Parameter(hidden = true) ModelMap model){
		if(StringUtils.isEmpty(city.getId())) {
			return ResultData.build().error( getResString("err.error", this.getResString("id")));
		}
		CityEntity _city = (CityEntity)cityBiz.getEntity(Integer.parseInt(city.getId()));
		return ResultData.build().success( _city);
	}


	@Operation(summary = "获取省市县镇村数据树形列表")
	@Parameters({
		@Parameter(name = "level", description = "省市县层级，默认为3", required = false, in = ParameterIn.QUERY),
		@Parameter(name = "type", description = "结构类型，默认为tree", required =  false, in = ParameterIn.QUERY)
	})
	@GetMapping("/query")
	@ResponseBody
	@Deprecated
	public ResultData query(HttpServletResponse response,HttpServletRequest request) {
		int level = BasicUtil.getInt("level",3);//默认3级
		String type = BasicUtil.getString("type","tree"); //默认为树形结构
		List<CityBean> cityList = (List<CityBean>) cityBiz.queryForTree(level,type);
		return ResultData.build().success(cityList);
	}





}
