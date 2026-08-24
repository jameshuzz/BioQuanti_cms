
package net.mingsoft.cms.constant.e;

import net.mingsoft.base.constant.e.BaseEnum;

/**
 * @Description: 栏目是否被搜索
 * @Date: Create in 2023/06/20 11:18
 */
public enum CategoryIsSearchEnum implements BaseEnum{
    /**
     * 启用
     */
    ENABLE("enable"),

    /**
     * 禁用
     */
    DISABLE("disable");

    CategoryIsSearchEnum(String status) {
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
