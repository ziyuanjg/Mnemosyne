package com.mnemosyne.master.receive;

import cn.hutool.core.collection.CollectionUtil;
import com.mnemosyne.common.BizResult;
import com.mnemosyne.common.Configuration;
import com.mnemosyne.common.httpClient.HTTPClient;
import com.mnemosyne.common.httpClient.HTTPExceptionEnum;
import com.mnemosyne.common.httpClient.RequestTypeEnum;
import com.mnemosyne.election.ElectionConfig;
import com.mnemosyne.task.TaskStatusEnum;
import com.mnemosyne.task.disk.MainIndexConfig;
import java.util.Collections;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import com.mnemosyne.task.Task;
import javax.ws.rs.core.MediaType;

/**
 * Created by Mr.Luo on 2018/5/3
 */
@Path("receve")
@Produces({"application/json"})
public class ReceiveTask {

    private final String RECEVE_TASK_URL = "receve/receveTask/";
    private final String RECEVE_FINISHED_TASK_URL = "receve/receveFinishedTask/";

    /**
     * 接收任务消息
     */
    @Path("receveTask")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public BizResult receveTask(Task task) {

        if (task == null) {
            return BizResult.createErrorResult(HTTPExceptionEnum.PARAM_ERROR_URL);
        }

        // 只有主节点才可以接收任务
        if (ElectionConfig.getMasterNode().equals(ElectionConfig.getLocalNode())) {
            ElectionConfig.getServiceNodeList().stream()
                    .forEach(serviceNode -> Configuration.getReceiveTaskThreadPool().receiveTask(task, serviceNode));

            if (task.getExcuteTime().getTime() <= Configuration.getAssignHandler().getWheelTime().getTime()) {
                Configuration.getAssignTaskThreadPool().assignTask(task);
            }

            // 添加任务依赖
            if(task.getWaitTaskId() != null){
                Task waitTask = Configuration.getTaskHandler().getTaskById(task.getWaitTaskId());
                if(waitTask != null){
                    waitTask.getPostpositivelyTaskIdList().add(task.getId());
                    ElectionConfig.getServiceNodeList().stream()
                            .forEach(serviceNode -> Configuration.getReceiveTaskThreadPool().receiveTask(waitTask, serviceNode));
                }
            }
        } else {
            // 子节点将任务转发给主节点，主要为了防止误请求到子节点。
            sendTaskToMaster(task);
        }
        // TODO 在什么地方添加任务执行中状态？申请的任务如果在执行中需保证不会二次执行
        return BizResult.createSuccessResult(null);
    }

    /**
     * 接收已完成任务信息，同步给各个子节点
     */
    @Path("receveFinishedTask")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public BizResult receveFinishedTask(Task task) {

        if (task == null) {
            return BizResult.createErrorResult(HTTPExceptionEnum.PARAM_ERROR_URL);
        }

        task.finish();

        // 只有主节点才可以接收任务
        if (ElectionConfig.getMasterNode().equals(ElectionConfig.getLocalNode())) {
            Configuration.getAssignTaskThreadPool().assignTask(task);

            // TODO 启动后续任务 需过滤执行中和未到时间任务
            if(CollectionUtil.isEmpty(task.getPostpositivelyTaskIdList())){

            }
        } else {
            // 子节点将任务转发给主节点，主要为了防止误请求到子节点。
            sendFinishedTaskToMaster(task);
        }

        return BizResult.createSuccessResult(null);
    }

    /**
     * 接收未完成任务信息，校验任务执行情况，如果已过执行时间则立即执行
     */
    @Path("receveUnfinishedTask")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public BizResult receveUnfinishedTask(List<Long> taskIdList){

        if(CollectionUtil.isEmpty(taskIdList)){
            return BizResult.createErrorResult(HTTPExceptionEnum.PARAM_ERROR_URL);
        }

        taskIdList.forEach(taskId -> {
            Task task = Configuration.getTaskHandler().getTaskById(taskId);
            if(task != null){
                if(task.getExcuteTime().getTime() < Configuration.getAssignHandler().getWheelTime().getTime()
                        && TaskStatusEnum.WAIT_RUN_STATUS.equals(task.getTaskStatusEnum())){
                    // 已过执行时间且为待执行状态
                    Configuration.getAssignTaskThreadPool().assignTask(task);
                }
            }
        });
        return BizResult.createSuccessResult(null);
    }

    private void sendFinishedTaskToMaster(Task task) {

        HTTPClient httpClient = Configuration.getHttpClient();
        String url = ElectionConfig.getMasterNode().getUrl() + RECEVE_FINISHED_TASK_URL;
        httpClient.send(url, null, task, RequestTypeEnum.POST);
    }

    private void sendTaskToMaster(Task task) {

        HTTPClient httpClient = Configuration.getHttpClient();
        String url = ElectionConfig.getMasterNode().getUrl() + RECEVE_TASK_URL;
        httpClient.send(url, null, task, RequestTypeEnum.POST);
    }
}
