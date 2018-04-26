package timeWheel;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;

/**
 * Created by 希罗 on 2018/4/25
 */
@Data
public class Task {

    /**
     * 链路下级任务
     */
    private Task lastTask;

    /**
     * 链路上级任务
     */
    private Task beforeTask;

    /**
     * 在链路中的位置
     */
    private Integer index;

    /**
     * 任务回调请求方式
     */
    private CallBackType callBackType;

    /**
     * 回调地址
     */
    private String url;

    /**
     * 回调参数
     */
    private JSONObject param;

    /**
     * 回调请求头
     */
    private String header;

    public Task(){

    }


}
