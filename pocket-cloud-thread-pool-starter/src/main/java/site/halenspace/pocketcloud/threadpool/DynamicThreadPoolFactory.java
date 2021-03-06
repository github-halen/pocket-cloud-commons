package site.halenspace.pocketcloud.threadpool;

import cn.hutool.core.thread.ThreadFactoryBuilder;
import lombok.extern.slf4j.Slf4j;
import site.halenspace.pocketcloud.threadpool.builder.ExecutorBlockingQueueBuilder;
import site.halenspace.pocketcloud.threadpool.builder.RejectedExecutionBuilder;
import site.halenspace.pocketcloud.threadpool.properties.ThreadPoolProperties;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadFactory;

/**
 * 动态线程池工厂类
 *
 * @author Halen.Leo · 2021/6/30
 * @blogger 后起小生
 * @github https://github.com/LeoHalen
 */
@Slf4j
public class DynamicThreadPoolFactory implements ThreadPoolFactory<DynamicThreadPoolExecutor, ThreadPoolProperties> {

    /**
     * 线程池名称容器
     */
    private static final Set<String> THREAD_POOL_NAME_SET = new HashSet<>();

    @Override
    public DynamicThreadPoolExecutor create(ThreadPoolProperties properties) {
        String threadPoolName = properties.getThreadPoolName();

        synchronized (THREAD_POOL_NAME_SET) {
            if (THREAD_POOL_NAME_SET.contains(threadPoolName)){
                // 如果存在
                threadPoolName += THREAD_POOL_NAME_SET.size();
            }
            THREAD_POOL_NAME_SET.add(threadPoolName);
        }

        DynamicThreadPoolExecutor dynamicThreadPoolExecutor = new DynamicThreadPoolExecutor(
                properties.getCorePoolSize(),
                properties.getMaximumPoolSize(),
                properties.getKeepAliveTime(),
                properties.getTimeUnit(),
                ExecutorBlockingQueueBuilder.build(properties.getQueueType(), properties.getQueueCapacity(), properties.isFair()),
                buildThreadFactory(threadPoolName),
                RejectedExecutionBuilder.build(properties.getRejectedExecutionType()));
        log.info("Dynamic thread pool factory: initialize {}(" +
                        "corePoolSize={},maximumPoolSize={},keepAliveTime={},timeUnit={},queue={},rejectedExecution={})",
                threadPoolName, properties.getCorePoolSize(), properties.getMaximumPoolSize(), properties.getKeepAliveTime(),
                properties.getTimeUnit(), properties.getQueueType(), properties.getRejectedExecutionType());
        return dynamicThreadPoolExecutor;
    }

    public ThreadFactory buildThreadFactory(String threadPoolNamePrefix) {
        return ThreadFactoryBuilder.create()
                .setNamePrefix(threadPoolNamePrefix + "-")
                .setDaemon(true).build();
    }

}
