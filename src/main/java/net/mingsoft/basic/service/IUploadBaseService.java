


package net.mingsoft.basic.service;

import net.mingsoft.base.entity.ResultData;
import net.mingsoft.basic.bean.UploadConfigBean;

import java.util.List;


/**
 * 抽象的上传类
 */
public abstract class IUploadBaseService {

    /**
     * 通用的上传方法
     *
     * @param bean 统一上传bean对象
     * @return 返回保存的文件路径
     */
    public abstract ResultData upload(UploadConfigBean bean);

    /**
     * 检测文件是否存在
     * @param realPath 绝对路径
     * @return 文件存在则返回true
     */
    public abstract boolean checkFileIfExist(String realPath);

    /**
     * 根据filePath删除文件或文件夹
     * @param filePathList 文件或文件夹路径
     *                 eg: file /upload/1/a.jpg
     *                 eg: dir /upload/1/
     */
    public abstract void delete(List<String> filePathList);

}
