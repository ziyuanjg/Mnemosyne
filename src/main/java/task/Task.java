package task;

import common.httpClient.RequestTypeEnum;
import java.io.Serializable;
import java.util.Date;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * Created by 希罗 on 2018/4/25
 */
@Data
@Builder
@AllArgsConstructor
public class Task implements Serializable{

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

}
