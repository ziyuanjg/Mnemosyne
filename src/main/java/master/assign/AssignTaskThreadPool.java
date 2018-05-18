package master.assign;

import common.Configuration;
import common.httpClient.HTTPClient;
import common.httpClient.RequestTypeEnum;
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

/**
 * Created by Mr.Luo on 2018/5/14
 */
public class AssignTaskThreadPool {

    private final ThreadPoolExecutor assignTaskPool = new ThreadPoolExecutor(MasterConfig.getCorePoolSize(),
            MasterConfig.getMaxPoolSize(), MasterConfig.getKeepAliveTime(), TimeUnit.SECONDS,
            new LinkedBlockingQueue<Runnable>());

    private final LinkedBlockingQueue<AssignDTO> assignQueue = new LinkedBlockingQueue<>();

    public AssignTaskThreadPool() {

        for (Integer i = 0; i < SlaveConfig.getMaxPoolSize(); i++) {

            AssignThread taskThread = new AssignThread(assignQueue);
            assignTaskPool.execute(taskThread);
        }
    }

    public void assignTask(Date date, Integer partition) {

        if (date == null || partition == null) {
            return;
        }

        assignQueue.add(new AssignDTO(date, partition));
    }

    private class AssignThread implements Runnable {

        private final String addTaskUrl = "/salve/execute";
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
                    httpClient.send(serviceNode.getUrl(), null, assignDTO.toMap(), RequestTypeEnum.POST);
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
}
