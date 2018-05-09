package slave;

import lombok.Builder;
import lombok.Builder.Default;

/**
 * Created by 希罗 on 2018/5/9
 */
@Builder
public class SlaveConfig {

    /**
     * 持久化线程池-核心线程数
     */
    @Default
    private static Integer corePoolSize = 10;

    /**
     * 持久化线程池-最大线程数
     */
    @Default
    private static Integer maxPoolSize = 30;

    /**
     * 持久化线程池-线程保活时间
     */
    @Default
    private static Long keepAliveTime = 300L;

    public static Integer getCorePoolSize() {
        return corePoolSize;
    }

    public static void setCorePoolSize(Integer corePoolSize) {
        SlaveConfig.corePoolSize = corePoolSize;
    }

    public static Integer getMaxPoolSize() {
        return maxPoolSize;
    }

    public static void setMaxPoolSize(Integer maxPoolSize) {
        SlaveConfig.maxPoolSize = maxPoolSize;
    }

    public static Long getKeepAliveTime() {
        return keepAliveTime;
    }

    public static void setKeepAliveTime(Long keepAliveTime) {
        SlaveConfig.keepAliveTime = keepAliveTime;
    }
}