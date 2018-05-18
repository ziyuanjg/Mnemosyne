package task;

import com.alibaba.fastjson.JSON;
import common.CallBackTypeEnum;
import common.httpClient.RequestTypeEnum;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;

/**
 * Created by Mr.Luo on 2018/4/25
 */
@Data
@Builder
@AllArgsConstructor
public class Task implements Serializable {

    private Integer id;

    /**
     * 链路上级任务
     */
    private transient Task beforeTask;

    /**
     * 任务回调请求方式
     */
    private CallBackTypeEnum callBackType;

    /**
     * 回调地址
     */
    private String url;

    /**
     * 回调参数
     */
    private Map<String, String> param;

    /**
     * 回调请求头
     */
    private Map<String, String> header;

    /**
     * 执行时间
     */
    private Date excuteTime;

    /**
     * 任务创建时间
     */
    private Date createTime;

    /**
     * 请求方式
     */
    private RequestTypeEnum requestTypeEnum;

    /**
     * 是否已执行
     */
    @Default
    private Boolean isFinished = Boolean.FALSE;

    public Map toMap() {
        Map map = new HashMap(10);
        map.put("beforeTask", JSON.toJSONString(beforeTask));
        map.put("callBackType", callBackType.getCode());
        map.put("url", url);
        map.put("param", JSON.toJSONString(param));
        map.put("header", JSON.toJSONString(header));
        map.put("excuteTime", excuteTime);
        map.put("createTime", createTime);
        map.put("requestTypeEnum", requestTypeEnum.getCode());
        return map;
    }

}
