
package net.mingsoft;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;

import java.util.logging.Logger;

@SpringBootApplication(scanBasePackages = {"net.mingsoft"})
@MapperScan(basePackages={"**.dao","com.baomidou.**.mapper"})
@ServletComponentScan(basePackages = {"net.mingsoft"})
public class MSApplication {
	public static void main(String[] args) {
		SpringApplication springApplication = new SpringApplication(MSApplication.class);
		springApplication.setBannerMode(Banner.Mode.OFF);
		ConfigurableApplicationContext configurableApplicationContext = springApplication.run(args);
		Environment env = configurableApplicationContext.getEnvironment();
		String port = env.getProperty("server.port", "8080");
		String managerPath = env.getProperty("ms.manager.path", "");
		String profiles = String.join(", ", env.getActiveProfiles());

		System.out.printf(
				"\n" +
						"\033[1;36m" + // 青色加粗
						"╔══════════════════════════════════════════════════════════╗\n" +
						"║ \033[1;33m🚀 BioQuanti CMS Application Started Successfully! \033[1;36m     ║\n" +
						"╠══════════════════════════════════════════════════════════╣\n" +
						"║ \033[0;32m➜ Manager URL: \033[0;33mhttp://localhost:%s\033[1;36m%s\033[1;36m/login.do         ║\n" +
						"║ \033[0;32m➜ Front URL: \033[0;33mhttp://localhost:%s/\033[1;36m                      ║\n" +
						"║ \033[0;32m➜ Activate Profiles: \033[0;35m%s\033[1;36m                                 ║\n" +
						"╚══════════════════════════════════════════════════════════╝" +
						"\033[0m", // 重置颜色
				port,
				managerPath,
				port,
				profiles
		);
	}
}


