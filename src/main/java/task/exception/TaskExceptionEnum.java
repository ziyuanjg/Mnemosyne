package task.exception;

import common.EnumInterface;

/**
 * Created by 希罗 on 2018/4/28
 */
public enum TaskExceptionEnum implements EnumInterface {

    FILE_CREATE_FAIL(1001, "文件创建失败"),
    FILE_GET_LOCK_FAIL(1002, "获取文件锁失败"),
    FILE_WRITE_FAIL(1003, "持久化任务失败"),

    PARAM_ERROR_PARTITION(1050, "缺少分区参数"),
    PARAM_ERROR_DATE(1051, "缺少日期参数")
    ;


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
