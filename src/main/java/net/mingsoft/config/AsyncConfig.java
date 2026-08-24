





package net.mingsoft.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 设置线程池配置
 * @author by 铭软开发团队
 * @Description TODO
 * @date 2019/11/20 15:20
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(AsyncConfig.class);

    @Value("${ms.thread.core-pool-size:10}")
    private int corePoolSize;

    @Value("${ms.thread.max-pool-size:50}")
    private int maxPoolSize;

    @Value("${ms.thread.queue-capacity:1000}")
    private int queueCapacity;

    @Value("${ms.thread.keep-alive-seconds:300}")
    private int KeepAliveSeconds;

    @Value("${ms.thread.thread-name-prefix:common-}")
    private String threadNamePrefix;

    @Value("${ms.thread.await-termination-seconds:60}")
    private int awaitTerminationSeconds;


    /**
     * 通用异步线程池 如果@Async注解没有指定线程池名称，则使用此线程池
     */
    @Bean
    public ThreadPoolTaskExecutor threadPoolTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 核心线程数：线程池创建时候初始化的线程数
        executor.setCorePoolSize(corePoolSize);
        // 最大线程数：线程池最大的线程数，只有在缓冲队列满了之后才会申请超过核心线程数的线程
        executor.setMaxPoolSize(maxPoolSize);
        // 缓冲队列：用来缓冲执行任务的队列
        executor.setQueueCapacity(queueCapacity);
        // 允许线程的空闲时间60秒：当超过了核心线程之外的线程在空闲时间到达之后会被销毁
        executor.setKeepAliveSeconds(KeepAliveSeconds);
        // 线程池名的前缀：设置好了之后可以方便我们定位处理任务所在的线程池
        executor.setThreadNamePrefix(threadNamePrefix);
        // 缓冲队列满了之后的拒绝策略：由调用线程处理（一般是主线程）
        executor.setAllowCoreThreadTimeOut(true);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(awaitTerminationSeconds);
        executor.setRejectedExecutionHandler((r, curExecutor) -> {
            LOGGER.debug("common-线程任务被拒绝，将由调用线程执行. 当前池状态: {}/{} 活跃, 队列: {}/{}",
                    curExecutor.getActiveCount(),
                    curExecutor.getMaximumPoolSize(),
                    curExecutor.getQueue().size(),
                    curExecutor.getQueue().remainingCapacity() + curExecutor.getQueue().size());

            // 仍然使用CallerRunsPolicy
            new ThreadPoolExecutor.CallerRunsPolicy().rejectedExecution(r, curExecutor);
        });
        executor.initialize();
        return executor;
    }
}
