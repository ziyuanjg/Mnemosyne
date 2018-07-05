package com.mnemosyne.task;

import lombok.Builder;
import lombok.Builder.Default;

/**
 * 持久化参数 Created by Mr.Luo on 2018/4/28
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
    @Default
    private static String filePath = "/Users/admin/mnemosyne";

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
     * 每条任务的最大长度
     */
    @Default
    private static Integer taskMAXLength = 1024;

    /**
     * 每个分区的任务数
     */
    @Default
    private static Integer taskNumOfPartition = 1000 * 5;

    public static Integer getTaskNumOfPartition() {
        return taskNumOfPartition;
    }

    public static void setTaskNumOfPartition(Integer taskNumOfPartition) {
        SaveConfig.taskNumOfPartition = taskNumOfPartition;
    }

    public static Integer getTaskMAXLength() {
        return taskMAXLength;
    }

    public static void setTaskMAXLength(Integer taskMAXLength) {
        SaveConfig.taskMAXLength = taskMAXLength;
    }

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
        if (!filePath.endsWith("/")) {
            filePath = filePath + "/";
        }
        SaveConfig.filePath = filePath;
    }
}
