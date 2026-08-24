



package net.mingsoft.mdiy.constant.e;

/**
 * @Description:
 * @Date: Create in 2020/06/23 14:18
 */
public enum  TagTypeEnum {
    /**
     * 单标签
     */
    SINGLE("single"),
    /**
     * 列表标签
     */
    LIST("list"),
    /**
     * 宏定义
     */
    MACRO("macro"),
    /**
     * 全局自定义标签
     */
    GLOBAL("global");


    TagTypeEnum(String type) {
        this.type = type;
    }

    private String type;

    public String getType() {
        return type;
    }

    public static TagTypeEnum get(String type) {
        for (TagTypeEnum e : TagTypeEnum.values()) {
            if (e.type.equals(type)) {
                return e;
            }
        }
        return null;
    }
}
