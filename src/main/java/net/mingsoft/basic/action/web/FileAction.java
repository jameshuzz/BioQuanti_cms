







package net.mingsoft.basic.action.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.mingsoft.base.entity.ResultData;
import net.mingsoft.basic.action.BaseFileAction;
import net.mingsoft.basic.annotation.LogAnn;
import net.mingsoft.basic.bean.UploadConfigBean;
import net.mingsoft.basic.constant.e.BusinessTypeEnum;
import net.mingsoft.basic.util.BasicUtil;
import net.mingsoft.config.MSProperties;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.File;
import java.io.IOException;

/**
 * 上传文件
 */
@Tag(name = "前端-基础接口")
@Controller
@RequestMapping("/file")
public class FileAction extends BaseFileAction {

	@Operation(summary =  "处理post请求上传文件")
	@Parameters({
			@Parameter(name = "uploadPath", description = "上传文件夹地址"),
			@Parameter(name = "file", description = "文件流"),
			@Parameter(name = "rename", description = "是否重命名", schema = @Schema(type = "boolean", defaultValue = "false")),
			@Parameter(name = "appId", description = "上传路径是否需要拼接appId", schema = @Schema(type = "boolean", defaultValue = "false")),
	})
	@PostMapping(value = "/upload")
	@ResponseBody
	@LogAnn(title = "处理前台post请求上传文件",businessType= BusinessTypeEnum.OTHER)
	public ResultData upload(@Parameter(hidden = true) UploadConfigBean bean, HttpServletRequest req, HttpServletResponse res) throws IOException {
		boolean uploadEnable = MSProperties.upload.enableWeb;

		// 上传是否启用
		if(!uploadEnable){
			return ResultData.build().error(getResString("upload.not.enable"));
		}

		//非法路径过滤
		if(bean.getUploadPath()!=null&&(bean.getUploadPath().contains("../")||bean.getUploadPath().contains("..\\"))){
			return ResultData.build().error(getResString("err.error", new String[]{getResString("file.path")}));
		}

		// web层上传文件必须拼接appId
		// 如果没传递uploadPath，则只拼接appId
		bean.setUploadPath(BasicUtil.getApp().getAppId() + File.separator + (StringUtils.isNotBlank(bean.getUploadPath()) ? bean.getUploadPath() : ""));

		UploadConfigBean config = new UploadConfigBean(bean.getUploadPath(),bean.getFile(),bean.isRename());

		return this.upload(config);

	}

}
