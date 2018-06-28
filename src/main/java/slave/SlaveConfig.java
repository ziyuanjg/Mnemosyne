package slave;

import lombok.Builder;
import lombok.Builder.Default;

/**
 * Created by Mr.Luo on 2018/5/9
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

    /**
     * 心跳间隔时间
     */
    private static Long heartTime = 5000L;

    /**
     * 允许连续心跳请求连续失败的最大次数
     */
    private static Integer failMaxNum = 3;

    public static Integer getFailMaxNum() {
        return failMaxNum;
    }

    public static void setFailMaxNum(Integer failMaxNum) {
        SlaveConfig.failMaxNum = failMaxNum;
    }

    public static Long getHeartTime() {
        return heartTime;
    }

    public static void setHeartTime(Long heartTime) {
        SlaveConfig.heartTime = heartTime;
    }

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
