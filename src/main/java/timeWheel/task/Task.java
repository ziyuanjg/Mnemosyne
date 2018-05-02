package timeWheel.task;

import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSONObject;
import java.io.Serializable;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import timeWheel.CallBackTypeEnum;

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
    private JSONObject param;

    /**
     * 回调请求头
     */
    private String header;

    /**
     * 执行时间
     */
    private Date excuteTime;

    @Override
    public String toString() {
        return "{" +
                "  url='" + url + '\'' +
                ", callBackType=" + callBackType.getCode() +
                ", param=" + param.toString() +
                ", header='" + header + '\'' +
                ", excuteTime=" + DateUtil.formatDateTime(excuteTime) +
                '}';
    }
}
