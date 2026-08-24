



package net.mingsoft.mdiy.util;

import java.util.List;
import java.util.Map;

/**
 * 自定义模型工具类
 */
public class ModelUtil {

    /**
     * 递归处理Map列表中的元素，将非"grid"类型的元素及其子元素收集到另一个列表中。
     * @param maps 原始的Map列表，其中每个Map代表一个节点，可能包含子节点。
     * @param fields 收集非"grid"类型元素的目标列表。
     */
    public static void recursionField(List<Map<String, Object>> maps, List<Map> fields) {
        for (Map<String, Object> element : maps) {
            // 处理当前节点

            if(element.get("type")!=null && !element.get("type").equals("grid")) {
                fields.add(element);
            }
            // 获取当前节点的子节点列表
            List<Map<String, Object>> children = (List<Map<String, Object>>) element.get("children");

            // 递归处理子节点
            if (children != null && !children.isEmpty()) {
                recursionField(children,fields);
            }
        }
    }
}
