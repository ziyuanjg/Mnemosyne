package slave;

import common.Configuration;
import common.httpClient.HTTPClient;
import election.ElectionConfig;
import java.util.Map.Entry;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import okhttp3.Headers;
import okhttp3.Headers.Builder;
import task.Task;
import task.TaskHandler;

/**
 * Created by Mr.Luo on 2018/5/9
 */
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

        private final String FINISHED_TASK_URL = "master/receveFinishedTask/";

        TaskThread(LinkedBlockingQueue<Task> taskQueue) {
            this.taskQueue = taskQueue;
        }

        @Override
        public void run() {

            Task task = null;

            while (true) {

                try {

                    if ((task = taskQueue.take()) != null) {

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

                        task.setIsFinished(Boolean.TRUE);
                        TaskHandler taskHandler = Configuration.getTaskHandler();
                        taskHandler.saveTask(task);

                        sendFinishedTaskToMaster(httpClient, task);
                    }

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        private void sendFinishedTaskToMaster(HTTPClient httpClient, Task task){

            task.setIsFinished(Boolean.TRUE);
            httpClient.send(ElectionConfig.getMasterNode().getUrl() + FINISHED_TASK_URL, null, task, task.getRequestTypeEnum());
        }
    }
}

