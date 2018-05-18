package common.httpClient;

import common.EnumInterface;

/**
 * Created by Mr.Luo on 2018/5/8
 */
public enum RequestTypeEnum implements EnumInterface {

    GET(1, "GET"),
    POST(2, "POST");

    private Integer code;
    private String message;

    RequestTypeEnum(Integer code, String message) {
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
