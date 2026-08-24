



package net.mingsoft.mdiy.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import net.mingsoft.base.entity.BaseEntity;

/**
 * 进度表实体
 *
 * 创建日期：2021-3-18 11:50:14<br/>
 * 历史修订：<br/>
 */
@TableName("mdiy_config")
public class ConfigEntity extends BaseEntity {

    private static final long serialVersionUID = 1616039414961L;

    /**
     * 是否能够删除 0-能删除 1-不能删除
     */
    @TableField(whereStrategy = FieldStrategy.NEVER)
    private int notDel;

    /**
     * 模型id
     */
    private String modelId;

    /**
     * 模型名称
     */
    private String configName;
    /**
     * 模型数据
     */
    private String configData;
    /**
     * 模型类型
     */
    private String configType;

    /**
     * 站点Id
     * 不归多租户管理，我们自己业务代码管理
     */
    private String appId;

    /**
     * 设置模型id
     */
    public String getModelId() {
        return modelId;
    }

    /**
     * 获取模型id
     */
    public void setModelId(String modelId) {
        this.modelId = modelId;
    }

    /**
     * 设置模型名称
     */

    public void setConfigName(String configName) {
        this.configName = configName;
    }

    /**
     * 获取模型名称
     */
    public String getConfigName() {
        return this.configName;
    }

    /**
     * 设置模型数据
     */
    public void setConfigData(String configData) {
        this.configData = configData;
    }

    /**
     * 获取模型数据
     */
    public String getConfigData() {
        return this.configData;
    }

    /**
     * 设置模型类型
     */
    public String getConfigType() {
        return configType;
    }

    /**
     * 获取模型类型
     */
    public void setConfigType(String configType) {
        this.configType = configType;
    }

    public int getNotDel() {
        return notDel;
    }

    public void setNotDel(int notDel) {
        this.notDel = notDel;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }
}
