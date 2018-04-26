package timeWheel;

/**
 * Created by 希罗 on 2018/4/25
 */
public enum CallBackType {


    HTTP(1, "通过httpClient发送回调"),
    EUREKA(2, "通过eureka发送回调");



    private Integer type;

    private String msg;

    CallBackType(Integer type, String msg){

        this.type = type;
        this.msg = msg;
    }
}
