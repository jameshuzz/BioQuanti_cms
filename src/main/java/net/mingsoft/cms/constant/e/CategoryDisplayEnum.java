

package net.mingsoft.cms.constant.e;

import net.mingsoft.base.constant.e.BaseEnum;

/**
 * @Description: 栏目是否显示枚举类
 * @Date: Create in 2023/03/24 14:18
 */
public enum CategoryDisplayEnum implements BaseEnum {

    /**
     * 启用
     */
    ENABLE("enable"),

    /**
     * 禁用
     */
    DISABLE("disable");

    CategoryDisplayEnum(String status) {
        this.status = status;
    }

    private String status;

    @Override
    public int toInt() {
        return 0;
    }

    @Override
    public String toString() {
        return status;
    }
}
