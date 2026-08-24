







package net.mingsoft.basic.constant.e;

import net.mingsoft.base.constant.e.BaseEnum;

/**
 * 允许删除标识
 * @author by 铭软开发团队
 * @Description TODO
 * @date 2019/11/20 10:34
 */
public enum NotDelEnum implements BaseEnum {
    DEL(0),
    /**
     * 不允许删除
     */
    NOT_DEL(1);
    private int del;
    NotDelEnum(int del) {
        this.del = del;
    }
    @Override
    public int toInt() {
        return this.del;
    }
}
