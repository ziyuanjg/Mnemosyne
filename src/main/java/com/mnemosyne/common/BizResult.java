package com.mnemosyne.common;

import lombok.Data;

/**
 * Created by Mr.Luo on 2018/5/14
 */
@Data
public class BizResult<T> {

    private Boolean success;
    private Integer code;
    private String msg;
    private T data;

    private BizResult(Boolean success, Integer code, String msg, T data) {
        this.success = success;
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public static BizResult createSuccessResult(Object data) {
        return new BizResult(Boolean.TRUE, null, null, data);
    }

    public static BizResult createSuccessResult(Object data, Integer code, String msg) {
        return new BizResult(Boolean.TRUE, code, msg, data);
    }

    public static BizResult createErrorResult(Integer code, String msg) {
        return new BizResult(Boolean.FALSE, code, msg, null);
    }

    public static BizResult createErrorResult(EnumInterface enumInterface) {
        return new BizResult(Boolean.FALSE, enumInterface.getCode(), enumInterface.getMessage(), null);
    }
}
