package master.receive;

import electon.ElectonConfig;
import electon.ServiceNode;
import java.util.List;
import java.util.function.Consumer;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import task.Task;

/**
 * Created by Mr.Luo on 2018/5/3
 */
@Path("master")
public class ReceiveTask {

    /**
     * 接收任务消息
     */
    @Path("receveTask")
    @POST
    public Task receveTask(Task task){

        // 只有主节点才可以接收任务
        if(ElectonConfig.getMasterNode().equals(ElectonConfig.getLocalNode())){

            List<ServiceNode> serviceNodeList = ElectonConfig.getServiceNodeList();
            serviceNodeList.stream().forEach(serviceNode -> {


            });

        }else {
            // 子节点将任务转发给主节点，主要为了防止误请求到子节点。
        }

        return null;
    }
}
