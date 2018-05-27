package master.receive;

import common.BizResult;
import common.Configuration;
import common.httpClient.HTTPClient;
import common.httpClient.HTTPExceptionEnum;
import common.httpClient.RequestTypeEnum;
import election.ElectionConfig;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import task.Task;

/**
 * Created by Mr.Luo on 2018/5/3
 */
@Path("master")
@Produces({"application/json"})
public class ReceiveTask {

    private final String RECEVE_TASK_URL = "master/receveTask/";
    private final String RECEVE_FINISHED_TASK_URL = "master/receveFinishedTask/";

    /**
     * 接收任务消息
     */
    @Path("receveTask")
    @POST
    public BizResult receveTask(Task task) {

        if(task == null){
            return BizResult.createErrorResult(HTTPExceptionEnum.PARAM_ERROR_URL);
        }

        // 只有主节点才可以接收任务
        if (ElectionConfig.getMasterNode().equals(ElectionConfig.getLocalNode())) {
            ElectionConfig.getServiceNodeList().stream()
                    .forEach(serviceNode -> Configuration.getReceiveTaskThreadPool().receiveTask(task, serviceNode));

            if(task.getExcuteTime().getTime() <= Configuration.getAssignHandler().getCURRENT_TIME().getTime()){
                Configuration.getAssignTaskThreadPool().assignTask(task);
            }
        } else {
            // 子节点将任务转发给主节点，主要为了防止误请求到子节点。
            sendTaskToMaster(task);
        }

        return BizResult.createSuccessResult(null);
    }

    /**
     * 接收已完成任务信息，同步给各个子节点
     * @param task
     * @return
     */
    @Path("receveFinishedTask")
    @POST
    public BizResult receveFinishedTask(Task task){

        if(task == null){
            return BizResult.createErrorResult(HTTPExceptionEnum.PARAM_ERROR_URL);
        }

        task.setIsFinished(Boolean.TRUE);

        // 只有主节点才可以接收任务
        if (ElectionConfig.getMasterNode().equals(ElectionConfig.getLocalNode())) {
            Configuration.getAssignTaskThreadPool().assignTask(task);
        } else {
            // 子节点将任务转发给主节点，主要为了防止误请求到子节点。
            sendFinishedTaskToMaster(task);
        }

        return BizResult.createSuccessResult(null);
    }

    private void sendFinishedTaskToMaster(Task task){

        HTTPClient httpClient = Configuration.getHttpClient();
        String url = ElectionConfig.getMasterNode().getUrl() + RECEVE_FINISHED_TASK_URL;
        httpClient.send(url, null, task.toMap(), RequestTypeEnum.POST);
    }

    private void sendTaskToMaster(Task task) {

        HTTPClient httpClient = Configuration.getHttpClient();
        String url = ElectionConfig.getMasterNode().getUrl() + RECEVE_TASK_URL;
        httpClient.send(url, null, task.toMap(), RequestTypeEnum.POST);
    }
}
