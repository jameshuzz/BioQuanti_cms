



package net.mingsoft.mdiy.constant.e;

/**
 * @Description:
 * @Date: Create in 2020/06/23 14:18
 */
public enum ConfigTypeEnum {

    /**
     * 全局配置
     */
    CONFIG("config"),

    /**
     * 全局标签
     */
    TAG("tag");


    ConfigTypeEnum(String type) {
        this.type = type;
    }

    private String type;

    public String getType() {
        return type;
    }

    public static ConfigTypeEnum get(String type) {
        for (ConfigTypeEnum e : ConfigTypeEnum.values()) {
            if (e.type.equals(type)) {
                return e;
            }
        }
        return null;
    }
}
