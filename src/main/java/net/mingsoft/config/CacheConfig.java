
package net.mingsoft.config;

import net.mingsoft.basic.service.IBaseCacheConfigService;
import net.mingsoft.basic.service.ICacheService;
import net.mingsoft.basic.service.LocalCacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * 初始化加载全部配置
 */
@Configuration("msCacheConfig")
public class CacheConfig {


    @Autowired
    private List<IBaseCacheConfigService> cacheConfigServices;
    @Bean
    public ApplicationRunner initializeCache() {
        return args -> {
            cacheConfigServices.forEach(IBaseCacheConfigService::load);
        };

    }

    /**
     * 默认本地单机缓存
     */
    @Bean
    @ConditionalOnMissingBean(ICacheService.class)
    public ICacheService localCacheService() {
        return new LocalCacheService();
    }
}
