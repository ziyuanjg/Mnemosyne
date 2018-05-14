package master.receive;

import common.BizResult;
import common.Configuration;
import common.httpClient.HTTPClient;
import common.httpClient.RequestTypeEnum;
import electon.ElectonConfig;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import task.Task;

/**
 * Created by Mr.Luo on 2018/5/3
 */
@Path("master")
@Produces({"application/xml", "application/json"})
public class ReceiveTask {

    private final String RECEVE_TASK_URL = "master/receveTask/";

    /**
     * 接收任务消息
     */
    @Path("receveTask")
    @POST
    public BizResult receveTask(Task task){

        // 只有主节点才可以接收任务
        if(ElectonConfig.getMasterNode().equals(ElectonConfig.getLocalNode())){
            ElectonConfig.getServiceNodeList().stream().forEach(serviceNode -> Configuration.getReceiveTaskThreadPool().receiveTask(task, serviceNode));
        }else {
            // 子节点将任务转发给主节点，主要为了防止误请求到子节点。
            sendTaskToMaster(task);
        }

        return BizResult.createSuccessResult(null);
    }

    private void sendTaskToMaster(Task task){

        HTTPClient httpClient = Configuration.getHttpClient();
        String url = ElectonConfig.getMasterNode().getUrl() + RECEVE_TASK_URL;
        httpClient.send(url, null, task.toMap(), RequestTypeEnum.POST);
    }
}
