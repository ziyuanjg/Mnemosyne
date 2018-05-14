package slave.exception;

import common.EnumInterface;

/**
 * Created by Mr.Luo on 2018/5/14
 */
public enum SlaveExceptionEnum implements EnumInterface {

    EXECUTE_ERROR(2001, "执行任务失败");


    private Integer code;
    private String message;

    SlaveExceptionEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }


    @Override
    public Integer getCode() {
        return null;
    }

    @Override
    public String getMessage() {
        return null;
    }
}
