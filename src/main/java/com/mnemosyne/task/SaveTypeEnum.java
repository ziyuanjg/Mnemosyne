package com.mnemosyne.task;

import com.mnemosyne.common.EnumInterface;

/**
 * 持久化方式 Created by Mr.Luo on 2018/4/28
 */
public enum SaveTypeEnum implements EnumInterface{

    DISK(1, "DISK"),
    REDIS(2, "REDIS"),
    MYSQL(3, "MYSQL");

    private Integer code;
    private String message;

    SaveTypeEnum(Integer code, String message) {
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
