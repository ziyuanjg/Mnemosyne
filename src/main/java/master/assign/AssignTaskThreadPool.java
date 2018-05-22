package master.assign;

import common.Configuration;
import common.httpClient.HTTPClient;
import common.httpClient.RequestTypeEnum;
import electon.ElectonConfig;
import electon.ServiceNode;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import lombok.Data;
import master.MasterConfig;
import slave.SlaveConfig;
import task.Task;

/**
 * Created by Mr.Luo on 2018/5/14
 */
public class AssignTaskThreadPool {

    private final ThreadPoolExecutor assignPool = new ThreadPoolExecutor(MasterConfig.getCorePoolSize(),
            MasterConfig.getMaxPoolSize(), MasterConfig.getKeepAliveTime(), TimeUnit.SECONDS,
            new LinkedBlockingQueue<Runnable>());

    private final ThreadPoolExecutor assignTaskPool = new ThreadPoolExecutor(MasterConfig.getCorePoolSize(),
            MasterConfig.getMaxPoolSize(), MasterConfig.getKeepAliveTime(), TimeUnit.SECONDS,
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

    public void assignTask(Task task){

        if(task == null){
            return;
        }

        assignTaskQueue.add(task);
    }

    private class AssignThread implements Runnable {

        private final String addTaskUrl = "/salve/executeByPartition";
        private LinkedBlockingQueue<AssignDTO> assignQueue;

        public AssignThread(LinkedBlockingQueue<AssignDTO> assignQueue) {
            this.assignQueue = assignQueue;
        }

        @Override
        public void run() {

            try {

                AssignDTO assignDTO = null;

                while ((assignDTO = assignQueue.take()) != null) {

                    ServiceNode serviceNode = Configuration.getChooseNode().choose();
                    HTTPClient httpClient = Configuration.getHttpClient();
                    httpClient.send(serviceNode.getUrl() + addTaskUrl, null, assignDTO.toMap(), RequestTypeEnum.POST);
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

        public Map toMap() {
            Map map = new HashMap(2);
            map.put("date", date);
            map.put("partition", partition);
            return map;
        }
    }

    private class AssignTaskThread implements Runnable{

        private LinkedBlockingQueue<Task> assignTaskQueue;
        private final String addTaskUrl = "/salve/executeByTask";

        public AssignTaskThread(LinkedBlockingQueue<Task> assignTaskQueue) {
            this.assignTaskQueue = assignTaskQueue;
        }

        @Override
        public void run() {

            try {

                Task task = null;

                while ((task = assignTaskQueue.take()) != null) {

                    HTTPClient httpClient = Configuration.getHttpClient();
                    if(task.getIsFinished()){
                        Task tasktmp = task;
                        ElectonConfig.getServiceNodeList().stream().forEach(serviceNode -> {
                            httpClient.send(serviceNode.getUrl() + addTaskUrl, null, tasktmp.toMap(), RequestTypeEnum.POST);
                        });
                    }else {
                        ServiceNode serviceNode = Configuration.getChooseNode().choose();
                        httpClient.send(serviceNode.getUrl() + addTaskUrl, null, task.toMap(), RequestTypeEnum.POST);
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
