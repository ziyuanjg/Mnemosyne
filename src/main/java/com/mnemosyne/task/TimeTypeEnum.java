package com.mnemosyne.task;

import com.mnemosyne.common.EnumInterface;

/**
 * Created by 希罗 on 2018/7/16
 */
public enum TimeTypeEnum implements EnumInterface {

    /**
     * 每过多长时间执行一次（不强制要求准点，按照执行任务的时间向后推导下次执行时间）
     * 例：每过一小时执行一次
     */
    MINUTE_TYPE(1, "分钟维度"),
    HOUR_TYPE(2, "小时维度"),
    DAY_TYPE(3, "天维度"),


    /**
     * 每个时间点执行一次（强制要求准点，任务执行时按照任务配置时间获取下一个最近的执行时间点为下次执行时间）
     * 例：每个小时执行一次
     */
    EVERY_MINUTE_TYPE(10, "分钟维度"),
    EVERY_HOUR_TYPE(20, "小时维度"),
    EVERY_DAY_TYPE(30, "天维度");



    private Integer code;
    private String message;

    TimeTypeEnum(Integer code, String message) {
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

