
package net.mingsoft.cms.bean;

import cn.hutool.json.JSONUtil;

import java.util.HashMap;

/**
 * 百度编辑器状态实体类
 * 返回类型一般为大概两种如：{state: "SUCCESS", url:"", type:"",...}，{state: "info"}用来返回错误信息
 */
public class EditorStateBean extends HashMap<String, Object> {

    /**
     * 状态
     */
    private boolean state  = true;

    /**
     * 状态信息，一般用来返回错误信息
     */
    private String info;

    public EditorStateBean() {
    }

    public EditorStateBean(boolean state) {
        this.state = state;
    }

    public EditorStateBean(boolean state, String info) {
        this.state = state;
        this.info = info;
    }

    public boolean isSuccess() {
        return this.state;
    }

    @Override
    public String toString() {
        if (this.state) {
            this.put("state", "SUCCESS");
        } else {
            this.put("state", this.info);
        }
        return JSONUtil.toJsonStr(this);
    }

}
