package com.mnemosyne.master.assign;

import com.mnemosyne.common.Configuration;
import com.mnemosyne.common.httpClient.HTTPClient;
import com.mnemosyne.common.httpClient.RequestTypeEnum;
import com.mnemosyne.election.ElectionConfig;
import com.mnemosyne.election.ServiceNode;
import com.mnemosyne.master.MasterConfig;
import com.mnemosyne.slave.SlaveConfig;
import com.mnemosyne.task.Task;
import java.util.Date;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import lombok.Data;

/**
 * Created by Mr.Luo on 2018/5/14
 */
public class AssignTaskThreadPool {

    private final ThreadPoolExecutor assignPool = new ThreadPoolExecutor(MasterConfig.getAssignCorePoolSize(),
            MasterConfig.getAssignMaxPoolSize(), MasterConfig.getAssignKeepAliveTime(), TimeUnit.SECONDS,
            new LinkedBlockingQueue<Runnable>());

    private final ThreadPoolExecutor assignTaskPool = new ThreadPoolExecutor(MasterConfig.getAssignCorePoolSize(),
            MasterConfig.getAssignMaxPoolSize(), MasterConfig.getAssignKeepAliveTime(), TimeUnit.SECONDS,
            new LinkedBlockingQueue<Runnable>());

    private final LinkedBlockingQueue<AssignDTO> assignQueue = new LinkedBlockingQueue<>();
    private final LinkedBlockingQueue<Task> assignTaskQueue = new LinkedBlockingQueue<>();

    public AssignTaskThreadPool() {

        for (Integer i = 0; i < SlaveConfig.getMaxPoolSize(); i++) {

            AssignThread assignThread = new AssignThread(assignQueue);
            assignPool.execute(assignThread);

            AssignTaskThread assignTaskThread = new AssignTaskThread(assignTaskQueue);
            assignTaskPool.execute(assignTaskThread);
        }
    }

    public void assignTask(Date date, Integer partition) {

        if (date == null || partition == null) {
            return;
        }

        assignQueue.add(new AssignDTO(date, partition));
    }

    public void assignTask(Task task) {

        if (task == null) {
            return;
        }

        assignTaskQueue.add(task);
    }

    private class AssignThread implements Runnable {

        private final String EXECUTE_BY_PARTITION_URL = "/salve/executeByPartition";
        private LinkedBlockingQueue<AssignDTO> assignQueue;

        public AssignThread(LinkedBlockingQueue<AssignDTO> assignQueue) {
            this.assignQueue = assignQueue;
        }

        @Override
        public void run() {

            try {

                AssignDTO assignDTO = null;

                while ((assignDTO = assignQueue.take()) != null && Configuration.getMasterNodeService().getMaster()) {

                    ServiceNode serviceNode = Configuration.getLoadStrategy().getSlaveNode();
                    HTTPClient httpClient = Configuration.getHttpClient();
                    httpClient.send(serviceNode.getUrl() + EXECUTE_BY_PARTITION_URL, null, assignDTO,
                            RequestTypeEnum.POST);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Data
    private class AssignDTO {

        private Date date;
        private Integer partition;

        public AssignDTO(Date date, Integer partition) {
            this.date = date;
            this.partition = partition;
        }
    }

    private class AssignTaskThread implements Runnable {

        private final String EXECUTE_BY_TASK_URL = "/salve/executeByTask";
        private LinkedBlockingQueue<Task> assignTaskQueue;

        public AssignTaskThread(LinkedBlockingQueue<Task> assignTaskQueue) {
            this.assignTaskQueue = assignTaskQueue;
        }

        @Override
        public void run() {

            try {

                Task task = null;

                while ((task = assignTaskQueue.take()) != null && Configuration.getMasterNodeService().getMaster()) {

                    HTTPClient httpClient = Configuration.getHttpClient();
                    if (task.isFinish()) {
                        Task tasktmp = task;
                        ElectionConfig.getServiceNodeList().stream().forEach(serviceNode -> {
                            httpClient.send(serviceNode.getUrl() + EXECUTE_BY_TASK_URL, null, tasktmp,
                                    RequestTypeEnum.POST);
                        });
                    } else {
                        ServiceNode serviceNode = Configuration.getLoadStrategy().getSlaveNode();
                        httpClient.send(serviceNode.getUrl() + EXECUTE_BY_TASK_URL, null, task, RequestTypeEnum.POST);
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
