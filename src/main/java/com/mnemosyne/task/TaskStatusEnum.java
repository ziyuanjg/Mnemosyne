package com.mnemosyne.task;

import com.mnemosyne.common.EnumInterface;

/**
 * Created by 希罗 on 2018/7/17
 */
public enum TaskStatusEnum implements EnumInterface {


    WAIT_RUN_STATUS(1, "待执行状态"),
    FINISH_STATUS(2, "已执行状态"),
    PAUSE_STATUS(3, "暂停状态"),
    CANCEL_STATUS(4, "取消状态");

    private Integer code;
    private String message;

    TaskStatusEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public Integer getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
