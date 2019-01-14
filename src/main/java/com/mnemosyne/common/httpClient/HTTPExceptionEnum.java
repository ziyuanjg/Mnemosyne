package com.mnemosyne.common.httpClient;

import com.mnemosyne.common.EnumInterface;

/**
 * Created by Mr.Luo on 2018/5/8
 */
public enum HTTPExceptionEnum implements EnumInterface {

    PARAM_ERROR_URL(2001, "参数错误");


    private Integer code;
    private String message;

    HTTPExceptionEnum(Integer code, String message) {
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
