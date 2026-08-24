
package net.mingsoft.basic.bean;

import org.springframework.web.multipart.MultipartFile;

/**
 * @Author: mingsoft
 * @Description: 统一上传bean对象
 * @Date: Create in 2023/04/07 9:55 代替基础上传的内置类
 */
public class UploadConfigBean {

    /**
     * 上传文件夹
     */
    private String uploadPath;

    private MultipartFile file;

    /**
     * 文件重命名
     */
    private boolean rename = true;
    /**
     * 上传地址拼接appId
     */
    private boolean appId = true;

    /**
     * 文件MD5标识符
     */
    private String fileIdentifier;
    /**
     * 文件名
     */
    private String fileName;
    /**
     * 文件大小
     */
    private long fileSize;

    /**
     * 文件夹id
     */
    private String folderId = "1";

    public UploadConfigBean() {
    }

    public UploadConfigBean(String uploadPath, MultipartFile file) {
        this.setUploadPath(uploadPath);
        this.setFile(file);
    }

    public UploadConfigBean(String uploadPath, MultipartFile file, boolean rename) {
        this.setUploadPath(uploadPath);
        this.setFile(file);
        this.setRename(rename);
    }

    public UploadConfigBean(String uploadPath, MultipartFile file, boolean rename, String fileIdentifier, String fileName, long fileSize) {
        this.uploadPath = uploadPath;
        this.file = file;
        this.rename = rename;
        this.fileIdentifier = fileIdentifier;
        this.fileName = fileName;
        this.fileSize = fileSize;
    }

    public boolean isAppId() {
        return appId;
    }

    public void setAppId(boolean appId) {
        this.appId = appId;
    }

    public boolean isRename() {
        return rename;
    }

    public void setRename(boolean rename) {
        this.rename = rename;
    }

    public String getUploadPath() {
        return uploadPath;
    }

    public void setUploadPath(String uploadPath) {
        this.uploadPath = uploadPath;
    }

    public MultipartFile getFile() {
        return file;
    }

    public void setFile(MultipartFile file) {
        this.file = file;
    }

    public String getFileIdentifier() {
        return fileIdentifier;
    }

    public void setFileIdentifier(String fileIdentifier) {
        this.fileIdentifier = fileIdentifier;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public String getFolderId() {
        return folderId;
    }

    public void setFolderId(String folderId) {
        this.folderId = folderId;
    }

}
