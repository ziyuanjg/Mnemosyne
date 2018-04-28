package timeWheel.exception;

import common.ExceptionInterface;

/**
 * Created by 希罗 on 2018/4/28
 */
public enum TaskException implements ExceptionInterface{

    FILE_CREATE_FAIL(1001, "文件创建失败"),
    FILE_GET_LOCK_FAIL(1002, "获取文件锁失败"),
    FILE_WRITE_FAIL(1003, "持久化任务失败")
    ;


    private Integer code;
    private String message;

    TaskException(Integer code, String message) {
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
