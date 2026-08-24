






package net.mingsoft.basic.strategy;

import net.mingsoft.basic.entity.ModelEntity;

import java.util.List;

/**
 * 菜单策略
 * 员工和管理员的菜单modelList 接口不一样，避免重写问题
 */
public interface IModelStrategy {
    List<ModelEntity> list();
}
