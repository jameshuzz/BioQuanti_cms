





package net.mingsoft.basic.aop;

import cn.hutool.core.io.FileTypeUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.FileNameUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.ZipUtil;
import net.mingsoft.base.entity.ResultData;
import net.mingsoft.base.exception.BusinessException;
import net.mingsoft.basic.bean.UploadConfigBean;
import net.mingsoft.config.MSProperties;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 验证zip是否合法的aop
 * 重写basic增加自定义配置
 */
@Component
@Aspect
public class FileVerifyAop extends BaseAop{

    private static final Logger LOGGER = LoggerFactory.getLogger(FileVerifyAop.class);

    @Value("${ms.upload.web-file-type:png,jpg,jpeg,webp}")
    private String webFileType;

    /**
     * 切入点
     */
    @Pointcut("execution(* net.mingsoft.basic.action.ManageFileAction.upload(..)) || " +
            "execution(* net.mingsoft.basic.action.ManageFileAction.uploadTemplate(..))")
    public void uploadPointCut(){}


    /**
     * 后台上传文件的时候，将验证zip里的文件
     * @param joinPoint
     * @return
     * @throws Throwable
     */
    @Around("uploadPointCut()")
    public Object uploadAop(ProceedingJoinPoint joinPoint) throws Throwable {
        UploadConfigBean bean = super.getType(joinPoint, UploadConfigBean.class);
        ResultData resultData = prepareUpload(bean,false);
        if (resultData.isSuccess()) {
            return joinPoint.proceed();
        }
        return resultData;
    }

    /**
     * web上传文件的时候，将验证zip里的文件
     * @param joinPoint
     * @return
     * @throws Throwable
     */
    @Around("execution(* net.mingsoft.basic.action.web.FileAction.upload(..))")
    public Object webUploadAop(ProceedingJoinPoint joinPoint) throws Throwable {
        UploadConfigBean bean = super.getType(joinPoint, UploadConfigBean.class);
        ResultData resultData = prepareUpload(bean,true);
        if (resultData.isSuccess()) {
            return joinPoint.proceed();
        }
        return resultData;
    }


    /**
     * 抽取共用文件上传检查
     * @param uploadConfigBean 上传文件对象
     */
    protected ResultData prepareUpload(UploadConfigBean uploadConfigBean,boolean isWeb) throws Exception {

        //非法路径过滤
        if(uploadConfigBean.getUploadPath()!=null&&(uploadConfigBean.getUploadPath().contains("../")||uploadConfigBean.getUploadPath().contains("..\\"))){
            return ResultData.build().error("文件上传路径错误");
        }

        //检查文件类型
        if (uploadConfigBean.getFile() == null) {
            return ResultData.build().error("文件不能为空!");
        }
        MultipartFile file = uploadConfigBean.getFile();
        //检查文件类型
        String uploadFileName = file.getOriginalFilename();
        if (FileNameUtil.containsInvalid(uploadFileName)){
            return ResultData.build().error("非法文件名格式!");
        }
        if (StringUtils.isBlank(uploadFileName) || StringUtils.isBlank(FileNameUtil.mainName(uploadFileName))) {
            return ResultData.build().error("文件名不能为空!");
        }
        if (uploadFileName.lastIndexOf(".") < 0) {
            LOG.info("文件名错误:{}", uploadFileName);
            return ResultData.build().error("文件名错误");
        }
        // TODO: 2025/7/24 注意抓取图片不会受spring上传大小控制
        long maxFileSize = MSProperties.upload.multipart.maxFileSize;
        // 检查文件大小 可能存在编辑器抓取图片导致文件大小超出限制
        if (file.getSize() > maxFileSize * 1024) {
            return ResultData.build().error("文件大小超出限制");
        }

        // 文件目录必须不含有.
        if (StringUtils.isNotBlank(uploadConfigBean.getUploadPath()) && uploadConfigBean.getUploadPath().contains(".")){
            return ResultData.build().error("文件上传路径错误");
        }

        String fileExt = FileNameUtil.extName(uploadFileName).toLowerCase();
        String mimeType = "";
        // 读文件流获取当前文件真实的mimeType
        try (InputStream inputStream = file.getInputStream()) {
            // 注意mimeType可能会有null的情况
            mimeType = FileTypeUtil.getType(inputStream,true);
        }

        String uploadFileDenied = MSProperties.upload.denied;
        if (StringUtils.isBlank(uploadFileDenied)) {
            uploadFileDenied = "exe,jsp";
        }
        // 禁止的文件类型
        String[] errorType = uploadFileDenied.split(",");
        for (String errType : errorType) {
            if (StrUtil.equalsIgnoreCase(errType, mimeType)) {
                LOG.info("文件后缀被拒绝:{}",mimeType);
                return ResultData.build().error(StrUtil.format("文件后缀被拒绝:{}",mimeType));
            }
            if (StrUtil.equalsIgnoreCase(errType, fileExt)) {
                LOG.info("文件类型被拒绝:{}",fileExt);
                return ResultData.build().error(StrUtil.format("文件类型被拒绝:{}",fileExt));
            }

        }

        // 如果是web层上传，则校验当前文件是否是允许上传类型
        if (isWeb) {
            List<String> webAllowFile = StrUtil.split(webFileType, ",");
            if (!webAllowFile.contains(fileExt)) {
                LOG.info("web层上传文件后缀被拒绝:{}",fileExt);
                return ResultData.build().error(StrUtil.format("文件后缀被拒绝:{}",fileExt));
            }
            if (StrUtil.isNotBlank(mimeType) && !webAllowFile.contains(mimeType)) {
                LOG.info("web层上传文件类型被拒绝:{}",fileExt);
                return ResultData.build().error(StrUtil.format("文件类型被拒绝:{}",fileExt));
            }
        }

        try {
            // ZIP 文件校验
            if ("zip".equalsIgnoreCase(mimeType) || "zip".equalsIgnoreCase(fileExt)) {
                checkZip(file, isWeb);
            }
        } catch (Exception e) {
            LOGGER.error("文件流处理异常", e);
            return ResultData.build().error(e.getMessage());
        }

        return ResultData.build().success();
    }

    /**
     * 检查压缩包
     */
    protected void checkZip(MultipartFile multipartFile, boolean isWeb) {
        // 创建临时解压文件夹
        File tempFilePath = FileUtil.mkdir(FileUtil.getTmpDirPath() + "/Zip" + IdUtil.simpleUUID());
        File zipFile = FileUtil.file(tempFilePath.getAbsolutePath() + "/" + IdUtil.simpleUUID() + ".zip");
        try (InputStream inputStream = multipartFile.getInputStream()) {
            FileUtils.copyInputStreamToFile(inputStream, zipFile);
            // 尝试解压缩文件
            try {
                ZipUtil.unzip(zipFile, tempFilePath, Charset.forName("GBK"));
            } catch (Exception e) {
                ZipUtil.unzip(zipFile, tempFilePath, StandardCharsets.UTF_8);
            }
            // 获取文件夹下所有文件
            List<File> files = FileUtil.loopFiles(tempFilePath);
            // 移除压缩包自身
            files.remove(zipFile);
            // 禁止上传的格式
            List<String> deniedList = StrUtil.split(MSProperties.upload.denied, ",");
            // web层允许上传的格式
            List<String> webAllowFile = StrUtil.split(webFileType, ",");
            for (File file : files) {
                // 文件的类型 受文件幻数影响
                String fileType = FileTypeUtil.getType(file).toLowerCase();
                // 文件后缀名
                String fileSuffixName = FileUtil.getSuffix(file);
                // 通过yml的配置检查文件格式是否合法
                if (deniedList.contains(fileSuffixName)) {
                    throw new BusinessException(StrUtil.format("压缩包内文件{}后缀{}禁止上传", file.getName(), fileSuffixName));
                } else if (deniedList.contains(fileType)) {
                    throw new BusinessException(StrUtil.format("压缩包内文件{}的类型{}禁止上传", file.getName(), fileType));
                }
                if (isWeb) {
                    if (!webAllowFile.contains(fileSuffixName)) {
                        LOG.info("web层上传文件后缀被拒绝:{}",fileSuffixName);
                        throw new BusinessException(StrUtil.format("压缩包内文件{}后缀{}禁止上传", file.getName(), fileSuffixName));
                    }
                    if (!webAllowFile.contains(fileType)) {
                        LOG.info("web层上传文件类型被拒绝:{}",fileType);
                        throw new BusinessException(StrUtil.format("压缩包内文件{}的类型{}禁止上传", file.getName(), fileType));
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            FileUtil.del(tempFilePath);
        }
    }

}
