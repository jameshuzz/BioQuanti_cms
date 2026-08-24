






package net.mingsoft.basic.strategy;

import cn.hutool.crypto.SecureUtil;
import net.mingsoft.basic.biz.IManagerBiz;
import net.mingsoft.basic.entity.ManagerEntity;
import net.mingsoft.basic.realm.CustomUserNamePasswordToken;
import net.mingsoft.basic.util.BasicUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 管理员登录列表
 *
 * @author Administrator
 * @version 创建日期：2020/11/18 18:12<br/>
 * 历史修订：<br/>
 */
public class ManagerLoginStrategy implements ILoginStrategy{


    @Autowired
    private IManagerBiz managerBiz;

    @Override
    public Boolean login(ManagerEntity manager) {
        managerBiz.updateCache();
        boolean rememberMe = BasicUtil.getBoolean("rememberMe");
        if(manager ==null || StringUtils.isEmpty(manager.getManagerName()) || StringUtils.isEmpty(manager.getManagerPassword())){
            return false;
        }
        // 根据账号获取当前管理员信息
        ManagerEntity newManager = new ManagerEntity();
        newManager.setManagerName(manager.getManagerName());
        ManagerEntity _manager = (ManagerEntity) managerBiz.getEntity(newManager);
        if (_manager == null ) {
            // 系统不存在此用户
            return false;
        } else {
            // 判断当前用户输入的密码是否正确
            // TODO 2025年8月15日18点01分 这里判断暂时不去除，ManagerRealm暂时不支持MD5比对加密，待优化
            if (SecureUtil.md5(manager.getManagerPassword()).equals(_manager.getManagerPassword())) {
                Subject subject = SecurityUtils.getSubject();
                CustomUserNamePasswordToken cupt = new CustomUserNamePasswordToken(manager.getManagerName(), _manager.getManagerPassword(), CustomUserNamePasswordToken.AuthType.MANAGER);
                cupt.setRememberMe(rememberMe);
                subject.login(cupt);
                return true;
            } else {
                // 密码错误
                return false;
            }
        }
    }
}
