






package net.mingsoft.basic.strategy;

import net.mingsoft.basic.entity.ManagerEntity;
import net.mingsoft.basic.entity.ModelEntity;

import java.util.List;

/**
 * 登录策略
 * 员工和管理员的登录 接口不一样，避免重写问题
 */
public interface ILoginStrategy {
    /**
     * 登录接口
     * @param manager 管理员账号信息
     * @return true 登录成功
     *  false 登录失败
     */
    Boolean login(ManagerEntity manager);
}
