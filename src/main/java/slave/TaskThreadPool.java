package slave;

import common.Configuration;
import common.httpClient.HTTPClient;
import java.util.Map.Entry;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import okhttp3.Headers;
import okhttp3.Headers.Builder;
import task.Task;
import task.TaskHandler;

/**
 * Created by 希罗 on 2018/5/9
 */
public class TaskThreadPool {


    private static final ThreadPoolExecutor pool = new ThreadPoolExecutor(SlaveConfig.getCorePoolSize(),
            SlaveConfig.getMaxPoolSize(), SlaveConfig.getKeepAliveTime(), TimeUnit.SECONDS,
            new LinkedBlockingQueue<Runnable>());

    private static final LinkedBlockingQueue<Task> taskQueue = new LinkedBlockingQueue<>();

    static {

        for(Integer i = 0; i < SlaveConfig.getMaxPoolSize(); i++){

            TaskThread taskThread = new TaskThread(taskQueue);
            pool.execute(taskThread);
        }
    }


    public void addTask(Task task) {

        if(task == null){
            return;
        }

        taskQueue.add(task);
    }

    private static class TaskThread implements Runnable {

        private LinkedBlockingQueue<Task> taskQueue;


        TaskThread(LinkedBlockingQueue<Task> taskQueue) {
            this.taskQueue = taskQueue;
        }

        @Override
        public void run() {

            Task task = null;

            while (true) {

                try {

                    if((task = taskQueue.take()) != null){

                        HTTPClient httpClient = Configuration.getHttpClient();
                        Headers headers = null;
                        if(task.getHeader() != null){
                            Headers.Builder builder = new Builder();
                            for(Entry<String, String> entry : task.getHeader().entrySet()){
                                builder.add(entry.getKey(), entry.getValue());
                            }
                            headers = builder.build();
                        }

                        httpClient.send(task.getUrl(), headers, task.getParam(), task.getRequestTypeEnum());

                        TaskHandler taskHandler = Configuration.getTaskHandler();
                        taskHandler.saveFinishTask(task);
                    }

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

