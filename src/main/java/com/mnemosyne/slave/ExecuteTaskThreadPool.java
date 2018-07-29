package com.mnemosyne.slave;

import cn.hutool.core.collection.CollectionUtil;
import com.mnemosyne.common.Configuration;
import com.mnemosyne.common.httpClient.HTTPClient;
import com.mnemosyne.common.httpClient.RequestTypeEnum;
import com.mnemosyne.election.ElectionConfig;
import com.mnemosyne.task.TaskStatusEnum;
import com.mnemosyne.task.disk.FileUtil;
import com.mnemosyne.task.disk.MainIndex;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Headers;
import okhttp3.Headers.Builder;
import com.mnemosyne.task.Task;
import com.mnemosyne.task.TaskHandler;

/**
 * Created by Mr.Luo on 2018/5/9
 */
@Slf4j(topic = "task")
public class ExecuteTaskThreadPool {


    private final ThreadPoolExecutor executeTaskPool = new ThreadPoolExecutor(SlaveConfig.getCorePoolSize(),
            SlaveConfig.getMaxPoolSize(), SlaveConfig.getKeepAliveTime(), TimeUnit.SECONDS,
            new LinkedBlockingQueue<Runnable>());

    private final LinkedBlockingQueue<Task> taskQueue = new LinkedBlockingQueue<>();

    public ExecuteTaskThreadPool() {

        for (Integer i = 0; i < SlaveConfig.getMaxPoolSize(); i++) {

            TaskThread taskThread = new TaskThread(taskQueue);
            executeTaskPool.execute(taskThread);
        }
    }

    public void addTask(Task task) {

        if (task == null) {
            return;
        }

        taskQueue.add(task);
    }

    private class TaskThread implements Runnable {

        private LinkedBlockingQueue<Task> taskQueue;

        private final String FINISHED_TASK_URL = "receve/receveFinishedTask/";

        private final String UNFINISHED_TASK_URL = "receve/receveUnfinishedTask/";


        TaskThread(LinkedBlockingQueue<Task> taskQueue) {
            this.taskQueue = taskQueue;
        }

        @Override
        public void run() {

            Task task = null;

            while (true) {

                try {

                    if ((task = taskQueue.take()) != null) {

                        if(task.isFinish()){
                            // 已经执行完毕
                            continue;
                        }

                        if(task.isPause()){
                            // 已暂停
                            continue;
                        }

                        Long waitTaskId;
                        if((waitTaskId = task.getWaitTaskId()) != null){
                            // 需判断前置任务是否完成
                            Task waitTask = Configuration.getTaskHandler().getTaskById(waitTaskId);
                            if(waitTask == null){
                                log.error("前置任务不存在,执行任务id:{},前置任务id:{}", task.getId(), waitTaskId);
                            } else if(!waitTask.isFinish()){
                                log.error("前置任务未完成,执行任务id:{},前置任务id:{}", task.getId(), waitTaskId);
                                sendUnFinishedTaskToMaster(CollectionUtil.newArrayList(waitTask.getId()));
                                continue;
                            }
                        }

                        HTTPClient httpClient = Configuration.getHttpClient();
                        Headers headers = null;
                        if (task.getHeader() != null) {
                            Headers.Builder builder = new Builder();
                            for (Entry<String, String> entry : task.getHeader().entrySet()) {
                                builder.add(entry.getKey(), entry.getValue());
                            }
                            headers = builder.build();
                        }

                        httpClient.send(task.getUrl(), headers, task.getParam(), task.getRequestTypeEnum());

                        task.finish();
                        TaskHandler taskHandler = Configuration.getTaskHandler();
                        taskHandler.saveTask(task);

                        sendFinishedTaskToMaster(task);

                        if(!CollectionUtil.isEmpty(task.getPostpositivelyTaskIdList())){
                            sendUnFinishedTaskToMaster(task.getPostpositivelyTaskIdList());
                        }
                    }

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        private void sendFinishedTaskToMaster(Task task){

            task.finish();
            Configuration.getHttpClient().send(ElectionConfig.getMasterNode().getUrl() + FINISHED_TASK_URL, null, task, RequestTypeEnum.POST);
        }

        private void sendUnFinishedTaskToMaster(List<Long> taskIdList){

            Configuration.getHttpClient().send(ElectionConfig.getMasterNode().getUrl() + UNFINISHED_TASK_URL, null, taskIdList, RequestTypeEnum.POST);
        }
    }
}

