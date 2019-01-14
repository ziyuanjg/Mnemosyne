package com.mnemosyne.task.exception;

import com.mnemosyne.common.EnumInterface;

/**
 * Created by Mr.Luo on 2018/4/28
 */
public enum TaskExceptionEnum implements EnumInterface {

    FILE_CREATE_FAIL(1001, "文件创建失败"),
    FILE_GET_LOCK_FAIL(1002, "获取文件锁失败"),
    FILE_WRITE_FAIL(1003, "持久化任务失败"),
    FILE_GET_CONFIG_FAIL(1004, "获取文件配置失败"),
    FILE_PARTITION_ERROR(1005, "分区选择错误"),
    FILE_TASK_ERROR(1006, "读取任务失败"),
    FILE_MAINCONFIG_WRITE_ERROR(1007, "写入主配置失败"),
    FILE_MAINCONFIG_READ_ERROR(1008, "读取主配置失败"),
    FILE_GET_MAIN_INDEX_CONFIG_ERROR(1009, "读取主索引配置信息失败"),
    FILE_SET_MAIN_INDEX_CONFIG_ERROR(1010, "写入主索引配置信息失败"),
    FILE_GET_MAIN_INDEX_ERROR(1010, "写入主索引信息失败"),
    FILE_SET_MAIN_INDEX_ERROR(1010, "写入主索引信息失败"),


    PARAM_ERROR_PARTITION(1050, "缺少分区参数"),
    PARAM_ERROR_DATE(1051, "缺少日期参数");


    private Integer code;
    private String message;

    TaskExceptionEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public Integer getCode() {
        return this.code;
    }

    @Override
    public String getMessage() {
        return this.message;
    }
}
