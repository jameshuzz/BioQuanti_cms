
package net.mingsoft.basic.service;

import java.util.List;
import java.util.Map;

/**
 * 基础信息格式化接口
 * 场景： 已知数据id 格式化展示标题
 * eg：管理员id 格式化成管理员昵称
 */
public interface IDataFormatterService {

    /**
     * 业务数据类型，如 "role" "manager" "people"等等
     */
    String dataType();


    /**
     * 数据格式化map
     * @param ids 需要格式化的数据id集合
     * @return key、value的格式化map
     */
    Map<String,String> format(List<String> ids);

}
