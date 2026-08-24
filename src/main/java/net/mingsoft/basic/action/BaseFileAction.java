



package net.mingsoft.basic.action;

import cn.hutool.core.io.FileTypeUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import net.mingsoft.base.constant.Const;
import net.mingsoft.base.entity.ResultData;
import net.mingsoft.basic.bean.UploadConfigBean;
import net.mingsoft.basic.util.BasicUtil;
import net.mingsoft.basic.util.ConfigUtil;
import net.mingsoft.config.MSProperties;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author by Administrator
 * @Description TODO
 * @date 2019/9/29 13:46
 */
public abstract class BaseFileAction extends BaseAction {

    /**
     * 统一上传文件方法
     * @param config 上传文件配置bean
     * @return ResultData
     * @throws IOException
     */
    public ResultData upload(UploadConfigBean config) throws IOException {
        return this.upload(config, false);
    }

    /**
     * 统一上传文件方法
     * @param config 上传文件配置bean
     * @param isRootUploadPath 是否从根路径开始上传
     *                         ture：则从根目录上传文件
     *                         false：则从配置文件夹下上传文件
     * @return ResultData
     * @throws IOException
     */
    public ResultData upload(UploadConfigBean config, boolean isRootUploadPath) throws IOException {
        // 上传路径
        String uploadPath = ConfigUtil.getString("文件上传配置", "uploadPath", MSProperties.upload.path);
        // 上传路径映射
        String uploadMapping = ConfigUtil.getString("文件上传配置", "uploadMapping", MSProperties.upload.mapping);

        //文件上传类型限制
        String fileName = config.getFile().getOriginalFilename();

        // 获取文件后缀
        String fileType =  FileUtil.getSuffix(config.getFile().getOriginalFilename());
        //word复制的时候的没有获取文件后缀，需要在根据内容里面的后缀进行获取
        if(StringUtils.isBlank(fileType)) {
            InputStream inputStream = config.getFile().getInputStream();
            fileType =  FileTypeUtil.getType(inputStream);
            inputStream.close();
        }
        boolean isReal = new File(uploadPath).isAbsolute();
        //绝对路径
        String realPath = isRootUploadPath ? BasicUtil.getRealPath("") : isReal ? uploadPath : BasicUtil.getRealPath(uploadPath);

        //修改文件名
        if(!config.isRename()){
            //Windows 系统下文件名最后会去掉. 这种文件默认拒绝  xxx.jsp. => xxx.jsp
            if (fileName.endsWith(".") && System.getProperty("os.name").startsWith("Windows")) {
                LOG.info("文件类型被拒绝:{}", fileName);
                return ResultData.build().error(getResString("err.error", getResString("file.type")));
            }
        }else {
            // 一次上传多个文件，时间戳可能重复，导致文件名重复
            fileName = IdUtil.getSnowflake().nextId() + "." + fileType;
        }

        // 上传的文件路径,判断是否填的绝对路径
        String uploadFolder = realPath + File.separator;
        //修改upload下的上传路径
        if (StringUtils.isNotBlank(config.getUploadPath())) {
            uploadFolder += config.getUploadPath() + File.separator;
        }

        //保存文件
        File saveFolder = new File(uploadFolder);
        File saveFile = new File(uploadFolder, fileName);
        if (!saveFolder.exists()) {
            FileUtil.mkdir(saveFolder);
        }
        doSaveFile(config, saveFile);
        //绝对映射路径处理
        //如果uploadFolderPath = true则返回路径中不拼upload的路径
        String path = (isRootUploadPath ? "" : uploadMapping.replace("**",""))
                //转为相对路径
                + uploadFolder.replace(realPath, "")
                //添加文件名
                + Const.SEPARATOR + fileName;
        //替换多余
        return ResultData.build().success(new File(Const.SEPARATOR + path).getPath().replace("\\", "/").replace("//", "/"));
    }

    public ResultData uploadTemplate(UploadConfigBean config) throws IOException {
        String uploadTemplatePath = ConfigUtil.getString("文件上传配置", "uploadTemplate", MSProperties.upload.template);

        //获取文件名字
        String fileName = config.getFile().getOriginalFilename();
        // 获取文件流，供获取类型使用
        InputStream inputStream = config.getFile().getInputStream();
        // 获取文件后缀
        String fileType = FileTypeUtil.getType(inputStream);
        inputStream.close();
        //判断上传路径是否为绝对路径
        boolean isReal = new File(uploadTemplatePath).isAbsolute();
        String realPath = null;
        if (!isReal) {
            //如果不是就获取当前项目路径
            realPath = BasicUtil.getRealPath("");
        } else {
            //如果是就直接取改绝对路径
            realPath = uploadTemplatePath;
        }
        //修改文件名
        if (!config.isRename()) {
            //Windows 系统下文件名最后会去掉. 这种文件默认拒绝  xxx.jsp. => xxx.jsp
            if (fileName.endsWith(".") && System.getProperty("os.name").startsWith("Windows")) {
                LOG.info("文件类型被拒绝:{}", fileName);
                return ResultData.build().error(getResString("err.error", getResString("file.type")));
            }
        } else {
            //取随机名
            fileName = System.currentTimeMillis() + "." + fileType;
        }

        // 上传的文件路径,判断是否填的绝对路径
        String uploadFolder = realPath + File.separator;
        //修改upload下的上传路径
        if(StringUtils.isNotBlank(config.getUploadPath()) && new File(config.getUploadPath()).isAbsolute()){
            uploadFolder = config.getUploadPath()+ File.separator;
        } else {
            uploadFolder += config.getUploadPath() + File.separator;
        }
        //保存文件
        File saveFolder = new File(uploadFolder);
        File saveFile = new File(uploadFolder, fileName);
        if (!saveFolder.exists()) {
            FileUtil.mkdir(saveFolder);
        }
        doSaveFile(config, saveFile);
        //绝对映射路径处理
        String path = uploadFolder.replace(realPath, "")
                //添加文件名
                + Const.SEPARATOR + fileName;
        //替换多余
        return ResultData.build().success(new File(Const.SEPARATOR + path).getPath().replace("\\", "/").replace("//", "/"));
    }


    /**
     * 文件落盘具体操作
     * @param config 上传文件配置对象
     * @param saveFile 文件存储路径
     */
    protected void doSaveFile(UploadConfigBean config,File saveFile) throws IOException {
        config.getFile().transferTo(saveFile);
    }
}
