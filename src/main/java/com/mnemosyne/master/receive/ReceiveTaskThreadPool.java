package com.mnemosyne.master.receive;

import com.mnemosyne.common.Configuration;
import com.mnemosyne.common.httpClient.HTTPClient;
import com.mnemosyne.common.httpClient.RequestTypeEnum;
import com.mnemosyne.election.ServiceNode;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import lombok.Data;
import com.mnemosyne.master.MasterConfig;
import com.mnemosyne.slave.SlaveConfig;
import com.mnemosyne.task.Task;

/**
 * Created by Mr.Luo on 2018/5/11
 */
public class ReceiveTaskThreadPool {


    private final ThreadPoolExecutor receiveTaskPool = new ThreadPoolExecutor(MasterConfig.getReceveCorePoolSize(),
            MasterConfig.getReceveMaxPoolSize(), MasterConfig.getReceveKeepAliveTime(), TimeUnit.SECONDS,
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

        private final String ADD_TASK_URL = "salve/add";
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
                    httpClient.send(serviceNode.getUrl() + ADD_TASK_URL, null, task, RequestTypeEnum.GET);
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
