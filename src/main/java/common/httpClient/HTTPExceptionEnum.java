package common.httpClient;

import common.EnumInterface;

/**
 * Created by 希罗 on 2018/5/8
 */
public enum HTTPExceptionEnum implements EnumInterface {

    PARAM_ERROR_URL(2001, "参数url为空或格式错误");


    private Integer code;
    private String message;

    HTTPExceptionEnum(Integer code, String message){
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
