




package net.mingsoft.config;

import com.jagregory.shiro.freemarker.ShiroTags;
import freemarker.core.TemplateClassResolver;
import freemarker.template.TemplateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import jakarta.annotation.PostConstruct;
import java.io.IOException;

@Configuration
public class FreemarkerConfig {

	@Autowired
    protected freemarker.template.Configuration configuration;
	@Autowired
	protected FreeMarkerConfigurer configurer;
    @Autowired
    protected org.springframework.web.servlet.view.freemarker.FreeMarkerViewResolver resolver;
    @Autowired
    protected org.springframework.web.servlet.view.InternalResourceViewResolver springResolver;

    @PostConstruct
	public void init() throws IOException, TemplateException {
        configuration.setNewBuiltinClassResolver(TemplateClassResolver.ALLOWS_NOTHING_RESOLVER);
		configuration.setSharedVariable("shiro", new ShiroTags());
        // 只有mvc视图转发需要这个配置，因为有spring环境，避免视图页面从spring上下文中获取到一些非预期的属性 比如 freemarker的默认配置 导致绕过我们的配置 可以执行危险指令
        resolver.setExposeSpringMacroHelpers(false);
	}

}
