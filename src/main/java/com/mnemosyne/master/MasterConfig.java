package com.mnemosyne.master;


import lombok.Builder;
import lombok.Builder.Default;

/**
 * Created by Mr.Luo on 2018/5/9
 */
@Builder
public class MasterConfig {


    /**
     * 分配任务线程池-核心线程数
     */
    @Default
    private static Integer assignCorePoolSize = 10;

    /**
     * 分配任务线程池-最大线程数
     */
    @Default
    private static Integer assignMaxPoolSize = 30;

    /**
     * 分配任务线程池-线程保活时间
     */
    @Default
    private static Long assignKeepAliveTime = 300L;

    /**
     * 接收任务线程池-核心线程数
     */
    @Default
    private static Integer receveCorePoolSize = 10;

    /**
     * 接收任务线程池-最大线程数
     */
    @Default
    private static Integer receveMaxPoolSize = 30;

    /**
     * 接收任务线程池-线程保活时间
     */
    @Default
    private static Long receveKeepAliveTime = 300L;

    public static Integer getReceveCorePoolSize() {
        return receveCorePoolSize;
    }

    public static void setReceveCorePoolSize(Integer receveCorePoolSize) {
        MasterConfig.receveCorePoolSize = receveCorePoolSize;
    }

    public static Integer getReceveMaxPoolSize() {
        return receveMaxPoolSize;
    }

    public static void setReceveMaxPoolSize(Integer receveMaxPoolSize) {
        MasterConfig.receveMaxPoolSize = receveMaxPoolSize;
    }

    public static Long getReceveKeepAliveTime() {
        return receveKeepAliveTime;
    }

    public static void setReceveKeepAliveTime(Long receveKeepAliveTime) {
        MasterConfig.receveKeepAliveTime = receveKeepAliveTime;
    }

    public static Integer getAssignCorePoolSize() {
        return assignCorePoolSize;
    }

    public static void setAssignCorePoolSize(Integer assignCorePoolSize) {
        MasterConfig.assignCorePoolSize = assignCorePoolSize;
    }

    public static Integer getAssignMaxPoolSize() {
        return assignMaxPoolSize;
    }

    public static void setAssignMaxPoolSize(Integer assignMaxPoolSize) {
        MasterConfig.assignMaxPoolSize = assignMaxPoolSize;
    }

    public static Long getAssignKeepAliveTime() {
        return assignKeepAliveTime;
    }

    public static void setAssignKeepAliveTime(Long assignKeepAliveTime) {
        MasterConfig.assignKeepAliveTime = assignKeepAliveTime;
    }

}
