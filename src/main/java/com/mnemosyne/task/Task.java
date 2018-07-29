package com.mnemosyne.task;

import com.mnemosyne.common.CallBackTypeEnum;
import com.mnemosyne.common.httpClient.RequestTypeEnum;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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

    private Long id;

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
     * 前置任务id
     */
    private Long waitTaskId;

//    /**
//     * 定时类型
//     */
//    private TimeTypeEnum timeTypeEnum;
//
//    /**
//     * 任务执行间隔时间
//     */
//    private Integer interval;

    /**
     * 后置任务id集合
     */
    private List<Long> postpositivelyTaskIdList;

    /**
     * 任务状态
     */
    @Default
    private TaskStatusEnum taskStatusEnum = TaskStatusEnum.WAIT_RUN_STATUS;

    public void finish(){
        this.taskStatusEnum = TaskStatusEnum.FINISH_STATUS;
    }

    public void cancel(){
        this.taskStatusEnum = TaskStatusEnum.CANCEL_STATUS;
    }

    public void pause(){
        this.taskStatusEnum = TaskStatusEnum.PAUSE_STATUS;
    }

    public Boolean isFinish(){
        return TaskStatusEnum.FINISH_STATUS.equals(this.taskStatusEnum);
    }

    public Boolean isCancel(){
        return TaskStatusEnum.CANCEL_STATUS.equals(this.taskStatusEnum);
    }

    public Boolean isPause(){
        return TaskStatusEnum.PAUSE_STATUS.equals(this.taskStatusEnum);
    }

    public List<Long> getPostpositivelyTaskIdList() {

        if(postpositivelyTaskIdList == null){
            synchronized (id){
                if(postpositivelyTaskIdList == null){
                    postpositivelyTaskIdList = new ArrayList<>(1);
                }
            }
        }
        return postpositivelyTaskIdList;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Task task = (Task) o;

        if (id != null ? !id.equals(task.id) : task.id != null) {
            return false;
        }
        return excuteTime != null ? excuteTime.equals(task.excuteTime) : task.excuteTime == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (id != null ? id.hashCode() : 0);
        result = 31 * result + (excuteTime != null ? excuteTime.hashCode() : 0);
        return result;
    }
}
