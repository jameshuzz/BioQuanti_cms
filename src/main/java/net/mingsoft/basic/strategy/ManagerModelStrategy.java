






package net.mingsoft.basic.strategy;

import net.mingsoft.basic.biz.IModelBiz;
import net.mingsoft.basic.constant.e.ManagerAdminEnum;
import net.mingsoft.basic.entity.ManagerEntity;
import net.mingsoft.basic.entity.ModelEntity;
import net.mingsoft.basic.util.BasicUtil;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * 管理员菜单列表,
 * @author Administrator
 * @version 创建日期：2020/11/18 18:12<br/>
 * 历史修订：<br/>
 */
public class ManagerModelStrategy implements IModelStrategy{

    /**
     * 注入模块业务层
     */
    @Autowired
    private IModelBiz modelBiz;

    @Override
    public List<ModelEntity> list() {
        ManagerEntity manager = BasicUtil.getManager();
        assert manager != null;
        List<ModelEntity> parentModelList;
        if (ManagerAdminEnum.SUPER.toString().equals(manager.getManagerAdmin())) {
            parentModelList = modelBiz.list();
        }else {
            parentModelList = modelBiz.queryModelByRoleId(manager.getRoleId());
        }
        return parentModelList;
    }
}
