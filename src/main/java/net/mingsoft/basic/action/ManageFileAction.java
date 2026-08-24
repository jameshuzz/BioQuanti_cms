







package net.mingsoft.basic.action;

import cn.hutool.core.io.FileTypeUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.FileNameUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.ZipUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.enums.ParameterStyle;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import net.mingsoft.base.entity.ResultData;
import net.mingsoft.base.exception.BusinessException;
import net.mingsoft.basic.annotation.LogAnn;
import net.mingsoft.basic.bean.UploadConfigBean;
import net.mingsoft.basic.constant.e.BusinessTypeEnum;
import net.mingsoft.basic.util.BasicUtil;
import net.mingsoft.config.MSProperties;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * 上传文件
 */
@Tag(name = "后端-基础接口")
@Controller("ManageFileAction")
@RequestMapping("${ms.manager.path}/file")
public class ManageFileAction extends BaseFileAction {



	/**
	 * 处理post请求上传文件
	 * 可以自定义项目路径下任意文件夹
	 * @param req
	 *            HttpServletRequest对象
	 * @param res
	 *            HttpServletResponse 对象
	 * @throws ServletException
	 *             异常处理
	 * @throws IOException
	 *             异常处理
	 */
	@Operation(summary =  "处理post请求上传文件")
	@LogAnn(title = "处理post请求上传文件",businessType= BusinessTypeEnum.OTHER)
	@Parameters({
			@Parameter(name = "uploadPath", description = "上传文件夹地址"),
			@Parameter(name = "file", description = "文件流"),
			@Parameter(name = "rename", description = "是否重命名", schema = @Schema(type = "boolean", defaultValue = "false")),
			@Parameter(name = "appId", description = "上传路径是否需要拼接appId", schema = @Schema(type = "boolean", defaultValue = "false")),
	})
	@PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@ResponseBody
	public ResultData upload(@Parameter(hidden = true) UploadConfigBean bean, HttpServletRequest req, HttpServletResponse res) throws IOException {
		//非法路径过滤
		if(checkUploadPath(bean)){
			return ResultData.build().error();
		}
		// 是否需要拼接appId
		if (bean.isAppId()) {
			// 如果没传递uploadPath，则只拼接appId
			bean.setUploadPath(BasicUtil.getApp().getAppId() + File.separator + (StringUtils.isNotBlank(bean.getUploadPath()) ? bean.getUploadPath() : ""));
		}

		UploadConfigBean config = new UploadConfigBean(bean.getUploadPath(),bean.getFile(),bean.isRename());
		return this.upload(config);
	}

	@Operation(summary =  "处理post请求上传模板文件")
	@Parameters({
			@Parameter(name = "uploadPath", description = "上传文件夹地址", required = false, in = ParameterIn.QUERY, style = ParameterStyle.FORM),
			@Parameter(name = "file", description = "文件流", required = false, style = ParameterStyle.FORM),
			@Parameter(name = "rename", description = "是否重命名", required =false, style = ParameterStyle.FORM, schema = @Schema(defaultValue = "true")),
			@Parameter(name = "appId", description = "上传路径是否需要拼接appId", required =false, style = ParameterStyle.FORM, schema = @Schema(defaultValue = "false")),
			@Parameter(name = "uploadFolderPath", description = "是否修改上传目录", required =false, style = ParameterStyle.FORM, schema = @Schema(defaultValue = "false")),
	})
	@PostMapping("/uploadTemplate")
	@ResponseBody
	@RequiresPermissions("basic:template:upload")
	@LogAnn(title = "处理post请求上传模板文件",businessType= BusinessTypeEnum.OTHER)
	public ResultData uploadTemplate(@Parameter(hidden = true) UploadConfigBean bean, HttpServletResponse res) throws IOException {
		String uploadTemplatePath = MSProperties.upload.template;
		//非法路径过滤
		if(checkUploadPath(bean)){
			return ResultData.build().error(getResString("err.error", new String[]{getResString("file.path")}));
		}
		if (StringUtils.isEmpty(bean.getUploadPath())) {
			bean.setUploadPath(uploadTemplatePath + File.separator +  BasicUtil.getApp().getAppId());
		} else if(!bean.getUploadPath().substring(0,uploadTemplatePath.length()).equalsIgnoreCase(uploadTemplatePath)){
			return ResultData.build().error("uploadPath参数错误");
		}

		UploadConfigBean config = new UploadConfigBean(bean.getUploadPath(), bean.getFile(), bean.isRename());
		ResultData resultData = this.uploadTemplate(config);
		if (!resultData.isSuccess()){
			// 上传失败
			return ResultData.build().error(resultData.getMsg());
		}
		String templateUrl = uploadTemplatePath + File.separator +  BasicUtil.getApp().getAppId();
		// 上传模板zip才解压
		if (!templateUrl.equals(bean.getUploadPath())){
			return ResultData.build().success();
		}
		// TODO: 2023/7/11  开始解压
		String fileUrl = (String) resultData.get(ResultData.DATA_KEY);
		//校验路径
		if (fileUrl != null && (fileUrl.contains("../") || fileUrl.contains("..\\"))) {
			return ResultData.build().error();
		}
		File zipFile = new File(BasicUtil.getRealTemplatePath(fileUrl));
		try {
			ZipUtil.unzip(zipFile.getPath(),zipFile.getParent(), Charset.forName("GBK"));
		} catch (Exception e) {
			ZipUtil.unzip(zipFile.getPath(),zipFile.getParent(), StandardCharsets.UTF_8);
		}
		FileUtil.del(zipFile);

		//删除分享模板下zip下的index.html文件\html\___data文件夹
		String htmlPath = zipFile.getParent()+"/html";
		if(FileUtil.exist(htmlPath)) {
			FileUtil.del(htmlPath);
		}

		String dataPath = zipFile.getParent()+"/data";
		if(FileUtil.exist(dataPath)) {
			FileUtil.del(dataPath);
		}

		//获取文件夹下所有文件
		List<File> files = FileUtil.loopFiles(zipFile.getParent());

		//禁止上传的格式
		List<String> deniedList = Arrays.stream(MSProperties.upload.denied.split(",")).map(String::toLowerCase).collect(Collectors.toList());
		for (File file : files) {
			FileInputStream fileInputStream = new FileInputStream(file);
			//文件的真实类型
			String fileType = FileTypeUtil.getType(file).toLowerCase();
			//通过yml的配置检查文件格式是否合法
			if (deniedList.contains(fileType) || deniedList.contains(FileNameUtil.extName(file).toLowerCase())){
				IOUtils.closeQuietly(fileInputStream);
				// 如果同目录或其他模板下存在不合法的文件类型，会清除站点下所有文件
				FileUtil.del(zipFile.getParent());
				throw new BusinessException(StrUtil.format("压缩包内文件{}文件异常",file.getName()));
			}
			IOUtils.closeQuietly(fileInputStream);
		}

		return ResultData.build().success();
	}

	protected boolean checkUploadPath(UploadConfigBean bean){
		return (bean.getUploadPath()!=null&&(bean.getUploadPath().contains("../")||bean.getUploadPath().contains("..\\")));
	}



}
