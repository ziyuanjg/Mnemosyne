package timeWheel.task;

import java.util.Date;
import java.util.List;

/**
 * Created by 希罗 on 2018/4/28
 */
public interface SaveTask {

    /**
     * 持久化任务数据
     * @param task
     * @return
     */
    Boolean saveTask(Task task);

    /**
     * 读取某一时间点的所有任务
     * @param date
     * @return
     */
    Task getTask(Date date);

    /**
     * 持久化已执行任务数据
     * @param task
     * @return
     */
    Boolean saveFinishTask(Task task);

}
