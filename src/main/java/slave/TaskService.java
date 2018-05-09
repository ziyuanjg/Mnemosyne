package slave;

import common.Configuration;
import java.util.Date;
import task.Task;
import task.TaskHandler;

/**
 * 任务处理器
 * Created by 希罗 on 2018/5/3
 */
public class TaskService {

    /**
     * 执行指定时间区任务
     * @param date
     */
    public void execute(Date date, Integer partition){

        TaskThreadPool taskThreadPool = Configuration.getTaskThreadPool();
        TaskHandler taskHandler = Configuration.getTaskHandler();

        Task task = taskHandler.getTask(date, partition);

        while(task != null){
            try {
                taskThreadPool.addTask(task);
            } catch (Exception e) {
                e.printStackTrace();
            }
            task = task.getBeforeTask();
        }
    }

    /**
     * 执行指定任务
     * @param task
     */
    public void execute(Task task){

        TaskThreadPool taskThreadPool = Configuration.getTaskThreadPool();
        try {
            taskThreadPool.addTask(task);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 新增任务
     * @param task
     */
    public void add(Task task){

        TaskHandler taskHandler = Configuration.getTaskHandler();
        taskHandler.saveTask(task);
    }


}
