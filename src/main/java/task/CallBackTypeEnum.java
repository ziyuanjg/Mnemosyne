package task;

/**
 * Created by 希罗 on 2018/4/25
 */
public enum CallBackTypeEnum {


    HTTP(1, "通过httpClient发送回调"),
    EUREKA(2, "通过eureka发送回调");



    private Integer code;

    private String msg;

    CallBackTypeEnum(Integer code, String msg){

        this.code = code;
        this.msg = msg;
    }

    public Integer getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
