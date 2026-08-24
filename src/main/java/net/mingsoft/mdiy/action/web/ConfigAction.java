



package net.mingsoft.mdiy.action.web;

import cn.hutool.json.JSONUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import net.mingsoft.base.entity.ResultData;
import net.mingsoft.mdiy.action.BaseAction;
import net.mingsoft.mdiy.biz.IModelBiz;
import net.mingsoft.mdiy.entity.ConfigEntity;
import net.mingsoft.mdiy.entity.ModelEntity;
import net.mingsoft.mdiy.util.ConfigUtil;
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
 * 自定义配置表管理控制层
 * @version
 * 版本号：1<br/>
 * 创建日期：2017-8-12 15:58:29<br/>
 * 历史修订：<br/>
 */
@Tag(name = "前端-自定义模块接口")
@Controller("webMdiyConfig")
@RequestMapping("/mdiy/config")
public class ConfigAction extends BaseAction {


	/**
	 * 注入自定义模型业务层
	 */
	@Autowired
	private IModelBiz modelBiz;

	/**
	 *  获取配置中的key指定value值
	 * @param configName 配置名称
	 * @param key 配置的key值
	 * @param response
	 * @param request
	 * @return
	 */
	@Operation(summary =  "获取配置中的key指定value值")
	@Parameters({
			@Parameter(name = "configName", description = "配置名称", required =  true, in = ParameterIn.QUERY),
			@Parameter(name = "key", description = "配置key", required =  true, in = ParameterIn.QUERY),
	})
	@GetMapping("/get")
	@ResponseBody
	public ResultData get(String configName,String key, HttpServletResponse response, HttpServletRequest request){
		if (StringUtils.isEmpty(configName) || StringUtils.isEmpty(key)) {
			return ResultData.build().error(getResString("err.empty",getResString("config.name")));
		}
		// 由于在站群环境下无法获取全局配置的模型数据，所以需要先从全局配置中获取模型id再去查询模型数据
		ConfigEntity configEntity = ConfigUtil.getEntity(configName);

		if(configEntity==null) {
			return ResultData.build().error(getResString("err.not.exist", this.getResString("config.name")));
		}

		ModelEntity modelEntity = modelBiz.getEntityById(configEntity.getModelId());


		if(modelEntity!=null) {
			//判断是否允许外部获取配置信息
			if(Boolean.parseBoolean(JSONUtil.toBean(modelEntity.getModelJson(), Map.class).get("isWebSubmit").toString())) {
				return ResultData.build().success(ConfigUtil.getString(configName,key));
			} else {
				return ResultData.build().error("此配置数据不允许外部获取");
			}
		}
		return ResultData.build().error(getResString("err.not.exist", this.getResString("config.name")));
	}

}
