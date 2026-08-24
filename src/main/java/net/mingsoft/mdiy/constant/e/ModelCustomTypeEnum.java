





package net.mingsoft.mdiy.constant.e;

import net.mingsoft.base.constant.e.BaseEnum;

/**
 * 自定义业务类型枚举
 * @author by 铭软开发团队
 * @Description TODO
 * @date 2022/05/31 16:34
 */
public enum ModelCustomTypeEnum implements BaseEnum {
    /**
     * 自定义配置
     */
    CONFIG("config"),

    /**
     * 自定义业务
     */
    FORM("form"),

    /**
     * 自定义模型
     */
    MODEL("model"),

    /**
     * 全局自定义标签
     */
    TAG("tag");

    /**
     * 枚举类型
     */
    public String label;

    ModelCustomTypeEnum(String label) {
        this.label = label;
    }

    public String getLabel() {
        return this.label;
    }

    @Override
    public int toInt() {
        return 0;
    }
}
