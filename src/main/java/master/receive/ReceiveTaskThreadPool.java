package master.receive;

import common.Configuration;
import common.httpClient.HTTPClient;
import common.httpClient.RequestTypeEnum;
import election.ServiceNode;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import lombok.Data;
import master.MasterConfig;
import slave.SlaveConfig;
import task.Task;

/**
 * Created by Mr.Luo on 2018/5/11
 */
public class ReceiveTaskThreadPool {


    private final ThreadPoolExecutor receiveTaskPool = new ThreadPoolExecutor(MasterConfig.getCorePoolSize(),
            MasterConfig.getMaxPoolSize(), MasterConfig.getKeepAliveTime(), TimeUnit.SECONDS,
            new LinkedBlockingQueue<Runnable>());

    private final LinkedBlockingQueue<ReceiveTaskDTO> receiveTaskQueue = new LinkedBlockingQueue<>();

    public ReceiveTaskThreadPool() {

        for (Integer i = 0; i < SlaveConfig.getMaxPoolSize(); i++) {

            ReceiveTaskThread taskThread = new ReceiveTaskThread(receiveTaskQueue);
            receiveTaskPool.execute(taskThread);
        }
    }

    public void receiveTask(Task task, ServiceNode serviceNode) {

        if (task == null || serviceNode == null) {
            return;
        }
        receiveTaskQueue.add(new ReceiveTaskDTO(task, serviceNode));
    }


    private class ReceiveTaskThread implements Runnable {

        private final String addTaskUrl = "/salve/add";
        private LinkedBlockingQueue<ReceiveTaskDTO> receiveTaskQueue = null;

        public ReceiveTaskThread(LinkedBlockingQueue<ReceiveTaskDTO> receiveTaskQueue) {
            this.receiveTaskQueue = receiveTaskQueue;
        }

        @Override
        public void run() {

            try {

                ReceiveTaskDTO receiveTaskDTO = null;

                while ((receiveTaskDTO = receiveTaskQueue.take()) != null) {

                    Task task = receiveTaskDTO.getTask();
                    ServiceNode serviceNode = receiveTaskDTO.getServiceNode();
                    HTTPClient httpClient = Configuration.getHttpClient();
                    httpClient.send(serviceNode.getUrl(), null, task.toMap(), RequestTypeEnum.GET);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Data
    private class ReceiveTaskDTO {

        private Task task;
        private ServiceNode serviceNode;

        public ReceiveTaskDTO(Task task, ServiceNode serviceNode) {
            this.task = task;
            this.serviceNode = serviceNode;
        }
    }
}
