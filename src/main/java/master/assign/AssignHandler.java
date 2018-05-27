package master.assign;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSON;
import common.Configuration;
import common.httpClient.HTTPClient;
import common.httpClient.RequestTypeEnum;
import election.ElectionConfig;
import election.ServiceNode;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import task.Task;
import task.TaskHandler;

/**
 * Created by Mr.Luo on 2018/5/9
 */
public class AssignHandler {

    private final DateTime CURRENT_TIME = DateUtil.date(new Date());


    public AssignHandler() {
        Date date = CURRENT_TIME.toJdkDate();
        Thread thread = new Thread(new AssignThread());
        thread.start();

        assignOldTask(date);

    }

    /**
     * 遍历当前时间之前的未执行任务，重新分配。 通常是宕机重启时执行
     */
    private void assignOldTask(Date date) {

        List<ServiceNode> serviceNodeList = ElectionConfig.getServiceNodeList();
        List<Task> allTaskList = new ArrayList<>();
        List<Task> retainAllTaskList = new ArrayList<>();
        serviceNodeList.stream().forEach(serviceNode -> {

            HTTPClient httpClient = Configuration.getHttpClient();
            String responseBody = httpClient
                    .send(serviceNode.getUrl() + "?date=" + date.getTime(), null, null, RequestTypeEnum.GET);
            List<Task> taskList = JSON.parseArray(responseBody, Task.class);

            if (retainAllTaskList.isEmpty() && allTaskList.isEmpty()) {
                retainAllTaskList.addAll(taskList);
            } else {
                retainAllTaskList.retainAll(taskList);
            }

            if (allTaskList.isEmpty()) {
                allTaskList.addAll(taskList);
            } else {
                taskList.removeAll(allTaskList);
                allTaskList.addAll(taskList);
            }
        });

        // 分发任务
        retainAllTaskList.stream().forEach(task -> Configuration.getAssignTaskThreadPool().assignTask(task));

        // 取未执行任务和全部任务的差集，分发给子节点，刷新执行状态
        allTaskList.removeAll(retainAllTaskList);
        allTaskList.stream().forEach(task -> {
            task.setIsFinished(Boolean.TRUE);
            Configuration.getAssignTaskThreadPool().assignTask(task);
        });
    }

    private class AssignThread implements Runnable {

        @Override
        public void run() {

            while (true) {
                try {
                    TaskHandler taskHandler = Configuration.getTaskHandler();
                    Integer count = taskHandler.getPartitionCount(CURRENT_TIME);
                    for (Integer i = 0; i < count; i++) {
                        Configuration.getAssignTaskThreadPool().assignTask(CURRENT_TIME, i);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                CURRENT_TIME.setField(DateField.SECOND, CURRENT_TIME.second() + 1);
            }
        }
    }

    public DateTime getCURRENT_TIME() {
        return CURRENT_TIME;
    }
}
