








package net.mingsoft.basic.action;

import cn.hutool.core.io.FileTypeUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.FileNameUtil;
import cn.hutool.core.io.file.FileReader;
import cn.hutool.core.io.file.FileWriter;
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
import net.mingsoft.basic.constant.e.CookieConstEnum;
import net.mingsoft.basic.util.BasicUtil;
import net.mingsoft.config.MSProperties;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author 铭软开发团队
 * @ClassName: TemplateAction
 * @Description: TODO(模板控制层)
 * @date 2020年7月2日
 */
@Tag(name = "后端-基础接口")
@Controller("/basicTemplate")
@RequestMapping("/${ms.manager.path}/basic/template")
public class TemplateAction extends BaseAction {

    /**
     * 返回主界面index
     */
    @Hidden
    @GetMapping("/index")
    @RequiresPermissions("basic:template:view")
    public String index(HttpServletResponse response, HttpServletRequest request) {
        return "/basic/template/index";
    }

    /**
     * 返回模板编辑
     */
    @Hidden
    @GetMapping("/form")
    @RequiresPermissions("basic:template:view")
    public String form(HttpServletResponse response, HttpServletRequest request) {
        return "/basic/template/form";
    }

    /**
     * 返回模板编辑页面
     */
    @Hidden
    @GetMapping("/edit")
    @RequiresPermissions("basic:template:view")
    public String edit(HttpServletResponse response, HttpServletRequest request) {
        return "/basic/template/edit";
    }

    /**
     * 点击模板管理，获取所有的模板文件名
     *
     * @param response 响应
     * @param request  请求
     * @return 返回模板文件名集合
     */
    @Operation(summary = "点击模板管理，获取所有的模板文件名")
    @Parameter(name = "pageNo", description = "pageNo，不填默认为1", required = false, in = ParameterIn.QUERY)
    @GetMapping("/queryTemplateSkin")
    @RequiresPermissions("basic:template:view")
    @ResponseBody
    protected ResultData queryTemplateSkin(HttpServletResponse response, HttpServletRequest request) {
        String pageNo = request.getParameter("pageNo");
        if (!NumberUtils.isCreatable(pageNo)) {
            pageNo = "1";
        }
        List<String> folderNameList = this.queryTemplateFile();
        Map<String, Object> map = new HashMap<>(3);
        map.put("folderNameList", folderNameList);
        map.put("pageNo", pageNo);
        BasicUtil.setCookie(response, CookieConstEnum.PAGENO_COOKIE, pageNo);
        return ResultData.build().success(map);
    }



    /**
     * http://localhost:5118/ms/file/uploadTemplate.do
     * 写入模板文件内容
     *
     * @param model
     * @param request  请求
     * @param response 响应
     * @throws IOException
     */
    @Operation(summary = "写入模板文件内容")
    @Parameters({
            @Parameter(name = "fileName", description = "文件名称", required = true, in = ParameterIn.QUERY),
            @Parameter(name = "fileContent", description = "文件内容", required = true, in = ParameterIn.QUERY),
    })
    @LogAnn(title = "写入模板文件内容", businessType = BusinessTypeEnum.UPDATE)
    @PostMapping("/writeFileContent")
    @ResponseBody
    @RequiresPermissions("basic:template:update")
    public ResultData writeFileContent(@Parameter(hidden = true) ModelMap model, HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        LOG.debug("ready modify template");
        String fileName = BasicUtil.getString("fileName");
        if (net.mingsoft.basic.util.FileUtil.isInvalidFileName(fileName)){
            return ResultData.build().error("非法路径");
        }
        // 文件路径
        String uploadTemplatePath = MSProperties.upload.template;
        String appId = BasicUtil.getApp().getAppId();
        String filePath = uploadTemplatePath + File.separator + appId + File.separator + fileName;
        String templatePath = BasicUtil.getRealTemplatePath(filePath);
        if (!FileUtil.exist(templatePath)) {
            return ResultData.build().error(this.getResString("failed.to.edit.a.template"));
        }
        //校验后缀文件名
        if (!checkFileType(fileName)) {
            return ResultData.build().error(this.getResString("failed.to.edit.a.template"));
        }

        String fileContent = BasicUtil.getString("fileContent");
        if (!StringUtils.isEmpty(filePath)) {
            FileWriter.create(new File(templatePath)).write(fileContent);
            LOG.debug("edit template file：{} success!",fileName);
            return ResultData.build().success();
        }
        return ResultData.build().error();
    }


    /**
     * 删除模版
     * <p>
     * 模版名称
     *
     * @param request 响应
     */
    @Operation(summary = "删除模版")
    @Parameter(name = "fileName", description = "模版名称", required = true, in = ParameterIn.QUERY)
    @LogAnn(title = "删除模版", businessType = BusinessTypeEnum.DELETE)
    @PostMapping("/delete")
    @ResponseBody
    @RequiresPermissions("basic:template:del")
    public ResultData delete(HttpServletRequest request) {
        String uploadTemplatePath = MSProperties.upload.template;
        String fileName = request.getParameter("fileName");
        if (net.mingsoft.basic.util.FileUtil.isInvalidFileName(fileName)) {
            return ResultData.build().error("非法路径");
        }
        String path = BasicUtil.getRealTemplatePath(uploadTemplatePath + File.separator
                + BasicUtil.getApp().getAppId() + File.separator + fileName);
        try {
            FileUtils.deleteDirectory(new File(path));
            return ResultData.build().success();
        } catch (Exception e) {
            return ResultData.build().error();
        }
    }


    /**
     * 显示子文件和子文件夹
     *
     * @param response 响应
     * @param request  请求
     * @return 返回文件名集合
     */
    @Operation(summary = "显示子文件和子文件夹")
    @Parameter(name = "skinFolderName", description = "skinFolderName", required = true, in = ParameterIn.QUERY)
    @GetMapping("/showChildFileAndFolder")
    @RequiresPermissions("basic:template:view")
    @ResponseBody
    public ResultData showChildFileAndFolder(HttpServletResponse response, HttpServletRequest request) {
        String uploadTemplatePath = MSProperties.upload.template;
        List<String> folderNameList = null;
        String skinFolderName = request.getParameter("skinFolderName");
        // 内部获取站点路径
        String appId = BasicUtil.getApp().getAppId();
        String uploadFileUrl =  uploadTemplatePath + File.separator + appId + File.separator + skinFolderName;
        String filter = BasicUtil.getRealTemplatePath(
                uploadTemplatePath + File.separator + BasicUtil.getApp().getAppId()+ File.separator);
        LOG.debug("过滤路径" + filter);
        // 非法路径过滤 避免被访问template上一层级目录
        if (net.mingsoft.basic.util.FileUtil.isInvalidFileName(skinFolderName)) {
            return ResultData.build().error("非法路径");
        }

        File files[] = new File(BasicUtil.getRealTemplatePath(uploadFileUrl)).listFiles();
        Map<String, Object> map = new HashMap<>();
        if (files != null) {
            folderNameList = new ArrayList<String>();
            List<String> fileNameList = new ArrayList<String>();
            for (int i = 0; i < files.length; i++) {
                File currFile = files[i];

                String temp = currFile.getPath();
                //以当前系统分隔符作判断，将不是当前系统的分隔符替换为当前系统的

                temp = temp.replace(File.separator.equals("\\") ? "/" : "\\", File.separator).replace(filter, "");
                if (currFile.isDirectory()) {
                    folderNameList.add(temp);
                } else {
                    fileNameList.add(temp);
                }
            }

            //记录文件夹数量
            map.put("folderNum", folderNameList.size());
            folderNameList.addAll(fileNameList);
            map.put("fileNameList", folderNameList);
        }
        map.put("uploadFileUrl", uploadFileUrl);
        // 压入模版路径，供前端显示图片使用
        map.put("templatePath", uploadFileUrl);
        return ResultData.build().success(map);
    }

    /**
     * 读取模版文件内容
     *
     * @param model
     * @param request 请求
     * @return 返回文件内容
     */
    @Operation(summary = "读取模版文件内容")
    @Parameter(name = "fileName", description = "文件名称", required = true, in = ParameterIn.QUERY)
    @GetMapping("/readFileContent")
    @ResponseBody
    @RequiresPermissions("basic:template:view")
    public ResultData readFileContent(@Parameter(hidden = true) ModelMap model, HttpServletRequest request) {
        String fileName = request.getParameter("fileName");
        String filePath = fileName;
        String uploadTemplatePath = MSProperties.upload.template;
        //非法路径过滤
        if (net.mingsoft.basic.util.FileUtil.isInvalidFileName(filePath)) {
            return ResultData.build().error("非法路径");
        }
        // 内部组织拼接模板路径，外部只负责传递文件名
        filePath = uploadTemplatePath + File.separator + BasicUtil.getApp().getAppId() + File.separator + filePath;
        String templatePath = BasicUtil.getRealTemplatePath(filePath);
        if (!FileUtil.exist(templatePath)) {
            return ResultData.build().error(getResString("err.not.exist", getResString("file.path")));
        }
        Map<String, Object> map = new HashMap<>();
        if (!StringUtils.isEmpty(filePath)) {
            map.put("fileContent", FileReader.create(new File(templatePath)).readString());
        }

        map.put("name", FileNameUtil.getName(fileName));
        map.put("fileName", fileName);
        map.put("fileNamePrefix", fileName.substring(0, fileName.lastIndexOf(File.separator) + 1));
        return ResultData.build().success(map);
    }

    /**
     * 删除模版文件
     * <p>
     * 文件名称
     *
     * @param request 请求
     */
    @Operation(summary =  "删除模版文件")
    @Parameter(name = "fileName", description = "文件名称", required =  true, in = ParameterIn.QUERY)
    @LogAnn(title = "删除模版文件", businessType = BusinessTypeEnum.DELETE)
    @PostMapping("/deleteTemplateFile")
    @ResponseBody
    @RequiresPermissions("basic:template:del")
    public ResultData deleteTemplateFile(HttpServletRequest request) {
        String uploadTemplatePath = MSProperties.upload.template;
        String fileName = request.getParameter("fileName");
        //非法路径过滤
        if (net.mingsoft.basic.util.FileUtil.isInvalidFileName(fileName)) {
            return ResultData.build().error("非法路径");
        }
        fileName = uploadTemplatePath + File.separator
                + BasicUtil.getApp().getAppId() + File.separator + fileName;
        FileUtil.del(BasicUtil.getRealTemplatePath(fileName));
        return ResultData.build().success();
    }

    /**
     * 递归获取所有的html\htm文件
     *
     * @param list    最终返回列表集合
     * @param fileDir 模版文件夹
     * @param style   风格
     */
    private void files(List list, File fileDir, String style) {
        if (fileDir.isDirectory()) {
            File files[] = fileDir.listFiles();
            for (int i = 0; i < files.length; i++) {
                File currFile = files[i];
                if (currFile.isFile()) {
                    String ex = currFile.getName();
                    if (ex.endsWith("htm") || ex.endsWith("html")) {
                        String _pathName = new String();
                        _pathName = files(currFile, style, _pathName);
                        list.add(_pathName + currFile.getName());
                    }
                } else if (currFile.isDirectory()) {
                    files(list, currFile, style);
                }
            }
        }
    }

    /**
     * 递归获取当前风格下所有的文件路径名称
     *
     * @param file
     * @param style
     * @param pathName
     * @return
     */
    private String files(File file, String style, String pathName) {
        if (!file.getParentFile().getName().equals(style)) {
            pathName = file.getParentFile().getName() + "/" + pathName;
            pathName = files(file.getParentFile(), style, pathName);
        }
        return pathName;
    }


    /**
     * 获取当前应用的所有的模版文件夹列表
     * @return 文件夹名称
     */
    private List<String> queryTemplateFile() {
        String uploadTemplatePath = MSProperties.upload.template;
        List<String> folderNameList = null;
        String _path = uploadTemplatePath + File.separator
                + BasicUtil.getApp().getAppId() + File.separator;
        LOG.debug("当前站点：{}" , BasicUtil.getApp().getAppName() );
        LOG.debug("当前站点模板路径_path：{}",_path);
        String templates = BasicUtil.getRealTemplatePath(_path);
        LOG.debug("当前站点模板路径：{}",templates);
        File file = new File(templates);
        LOG.debug("是否存在：{}",file.exists());
        String[] str = file.list();
        if (str != null) {
            folderNameList = new ArrayList<>();
            for (int i = 0; i < str.length; i++) {
                // 避免不为文件夹的文件显示
                if (str[i].indexOf(".") < 0) {
                    folderNameList.add(str[i]);
                }
            }
        }
        return folderNameList;
    }

    /**
     * 校验文件后缀名是否符合要求
     *
     * @param fileName 文件名
     * @return false 不合法 true 符合
     */
    protected boolean checkFileType(String fileName) {
        String uploadFileDenied = MSProperties.upload.denied;
        //校验后缀文件名
        String[] errorType = uploadFileDenied.split(",");
        String fileType = FileUtil.getSuffix(fileName);
        if (StringUtils.isBlank(fileType)){
            return false;
        }
        for (String type : errorType) {
            //校验禁止上传的文件后缀名（忽略大小写）
            if ((fileType).equalsIgnoreCase(type)) {
                LOG.info("文件类型被拒绝:{}", fileName);
                return false;
            }
        }
        return true;
    }


    /**
     * 接口：获取当前应用下的模版文件夹列表,提供给应用设置页面调用
     *
     * @param request 请求
     * @return 模版文件集合
     */
    @Operation(summary = "查询模版风格供站点选择")
    @GetMapping("/queryAppTemplateSkin")
    @ResponseBody
    public ResultData queryAppTemplateSkin(HttpServletRequest request) {
        List<String> folderNameList = this.queryTemplateFile();
        Map map = new HashMap();
        if (folderNameList != null) {
            map.put("appTemplates", folderNameList);
        }
        return ResultData.build().success(map);
    }


    /**
     * 接口：获取指定模下面所有的模版文件
     *
     * @param request
     * @return
     */
    @Operation(summary = "查询模版文件供栏目选择，可指定模板名称，不传查询应用设置中选择的模板")
    @GetMapping("/queryTemplateFileForColumn")
    @Parameters({
            @Parameter(name = "appStyle", description = "可选，可以指定template下的文件夹名称", required = false, in = ParameterIn.QUERY),
    })
    @ResponseBody
    public ResultData queryTemplateFileForColumn(HttpServletRequest request) {
        String uploadTemplatePath = MSProperties.upload.template;
        //优先 appStyle 接口传递过来的 模版风格
        String appStyle = BasicUtil.getString("appStyle", BasicUtil.getApp().getAppStyle());
        if (StringUtils.isBlank(appStyle)){
            return ResultData.build().error();
        }
        String path = BasicUtil.getRealTemplatePath(uploadTemplatePath + File.separator + BasicUtil.getApp().getAppId() + File.separator);

        List<File> list = FileUtil.loopFiles(path + appStyle, new FileFilter() {
            @Override
            public boolean accept(File pathname) {

                //遇到文件乱码，获取类型会失败，需要进行异常捕获
                try {
                    if (FileTypeUtil.getType(pathname).equalsIgnoreCase("html") || FileTypeUtil.getType(pathname).equalsIgnoreCase("htm")) {
                        return true;
                    } else {
                        return false;
                    }
                } catch (Exception e){
                    return false;
                }

            }
        });

        List<String> collect = list.stream().map(file -> {
            return file.getPath().replaceAll("\\\\","/").replace(path.replaceAll("\\\\","/"), "").substring(appStyle.length() + 1);
        }).collect(Collectors.toList());

        return ResultData.build().success(collect);
    }



}
