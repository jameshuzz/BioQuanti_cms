






package net.mingsoft.basic.bean;

import cn.hutool.crypto.SecureUtil;
import net.mingsoft.basic.entity.ManagerEntity;

/**
 * @Author: xierz
 * @Description:
 * @Date: Create in 2021/03/13 11:55
 */
public class ManagerModifyPwdBean extends ManagerEntity {
    //输入的旧密码
    private String oldManagerPassword;
    //输入的新密码
    private String newManagerPassword;

    public String getOldManagerPassword() {
        return oldManagerPassword;
    }

    public void setOldManagerPassword(String oldManagerPassword) {
        super.setManagerPassword(oldManagerPassword);
        this.oldManagerPassword = oldManagerPassword;
    }

    public String getNewManagerPassword() {
        return newManagerPassword;
    }

    public void setNewManagerPassword(String newManagerPassword) {
        this.newManagerPassword = newManagerPassword;
    }


}
