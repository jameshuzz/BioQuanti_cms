


package net.mingsoft.config;

import jakarta.annotation.Resource;
import jakarta.servlet.MultipartConfigElement;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.unit.DataSize;

/**
 * 由于springboot版本问题，上传文件大小配置在之前CustomMultipartResolver中不在支持，所以注入springboot上传配置
 * @author 铭软开发团队
 * @ClassName: UploadConfig
 * @Description: 控制上传文件大小配置
 * @date 2025-4-9 14:49:08
 */
@Configuration
public class MultipartConfig {

    @Resource
    private MSProperties msProperties;

    @Bean
    public MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();

        // -1为不限制，所以这里特殊处理
        long maxFileSize = msProperties.getUpload().multipart.getMaxFileSize();
        long maxRequestSize = msProperties.getUpload().multipart.getMaxRequestSize();

        // 总请求最大
        factory.setMaxFileSize(DataSize.ofKilobytes(maxFileSize));

        // 单个文件最大
        factory.setMaxRequestSize(DataSize.ofKilobytes(maxRequestSize));

        factory.setLocation(msProperties.getUpload().getMultipart().getUploadTempDir());

        return factory.createMultipartConfig();
    }

}
