package timeWheel;

import lombok.Builder;
import lombok.Builder.Default;
import timeWheel.task.SaveTypeEnum;

/**
 * 持久化参数
 * Created by 希罗 on 2018/4/28
 */
@Builder
public class SaveConfig {

    /**
     * 持久化方式
     */
    private static SaveTypeEnum saveTypeEnum;

    /**
     * 磁盘持久化目录
     */
    private static String filePath;

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
        SaveConfig.corePoolSize = corePoolSize;
    }

    public static Integer getMaxPoolSize() {
        return maxPoolSize;
    }

    public static void setMaxPoolSize(Integer maxPoolSize) {
        SaveConfig.maxPoolSize = maxPoolSize;
    }

    public static Long getKeepAliveTime() {
        return keepAliveTime;
    }

    public static void setKeepAliveTime(Long keepAliveTime) {
        SaveConfig.keepAliveTime = keepAliveTime;
    }

    public static SaveTypeEnum getSaveTypeEnum() {
        return saveTypeEnum;
    }

    public static void setSaveTypeEnum(SaveTypeEnum saveTypeEnum) {
        SaveConfig.saveTypeEnum = saveTypeEnum;
    }

    public static String getFilePath() {
        return filePath;
    }

    public static void setFilePath(String filePath) {
        if(!filePath.endsWith("/")){
            filePath = filePath + "/";
        }
        SaveConfig.filePath = filePath;
    }
}
