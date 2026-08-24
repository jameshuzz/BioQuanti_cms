
package net.mingsoft.mdiy.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import net.mingsoft.basic.util.SpringUtil;
import net.mingsoft.mdiy.biz.IConfigBiz;
import net.mingsoft.mdiy.biz.ITagBiz;
import net.mingsoft.mdiy.constant.Const;
import net.mingsoft.mdiy.constant.e.ConfigTypeEnum;
import net.mingsoft.mdiy.constant.e.ModelCustomTypeEnum;
import net.mingsoft.mdiy.constant.e.TagTypeEnum;
import net.mingsoft.mdiy.entity.ConfigEntity;
import net.mingsoft.mdiy.entity.TagEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 铭软开发团队
 * @ClassName:
 * @Description: 用来处理全局标签返回数数组问题，由数组Map转换为Map返回
 * @date
 */
@Service
public class GlobalTagService extends BaseTagClassService{

    @Override
    public Object excute(Map map) {
        //读取标签
        ITagBiz tagBiz = SpringUtil.getBean(ITagBiz.class);
        // 从map中获取站点id，因为在多线程中通过getWebsiteApp方法获取不到app数据，所以只能从map中获取，但是Basic.getApp()可以的，但是这里就不麻烦了。
        String appId = MapUtil.getStr(map, "appId");
        // 全局标签可能会多个
        TagEntity tag = new TagEntity();
        tag.setTagClass("globalTagService");
        tag.setTagType(TagTypeEnum.GLOBAL.getType());
        // 获取map中的appId，如果没有，则说明是非站群下，不需要拼接
        if (StrUtil.isNotBlank(appId)) {
            tag.setAppId(appId);
        }
        // 获取标签数据
        List<TagEntity> tagList = tagBiz.queryAll(tag);

        try {
            LambdaQueryWrapper<ConfigEntity> configWrapper = new LambdaQueryWrapper<>();
            configWrapper.eq(ConfigEntity::getConfigType, ConfigTypeEnum.TAG.getType())
                    .and(StrUtil.isNotBlank(appId), wrapper -> wrapper.eq(ConfigEntity::getAppId, Const.GLOBAL).or().eq(ConfigEntity::getAppId, appId));
            // 获取全局数据
            IConfigBiz configBiz = SpringUtil.getBean(IConfigBiz.class);
            List<ConfigEntity> configEntityList = configBiz.list(configWrapper);

            // 目前还是把数据全部设置到相对应的标签里，供前端获取使用
            if (CollUtil.isNotEmpty(configEntityList) && CollUtil.isNotEmpty(tagList)) {
                // 统一获取全局标签数据
                Map dataMap = new HashMap<>();
                for (TagEntity tagEntity : tagList) {
                    // 获取之前的自定义全局数据
                    Map tempMap = MapUtil.get(map, tagEntity.getTagName(), Map.class);
                    if (CollUtil.isNotEmpty(tempMap)) {
                        dataMap.putAll(tempMap);
                    }
                    // 获取全局标签数据
                    configEntityList.forEach(configEntity -> {
                        // 把字符串改成map,以便取值
                        dataMap.put(configEntity.getConfigName(), JSONUtil.toBean(configEntity.getConfigData(), Map.class));
                    });
                }
                return dataMap;
            }
        } catch (Exception e) {
            e.printStackTrace();
            LOG.error("获取全局标签数据错误");
        }

        return null;
    }
}
