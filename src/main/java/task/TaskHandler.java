package task;

import java.util.Date;

/**
 * Created by 希罗 on 2018/4/28
 */
public interface TaskHandler {

    /**
     * 持久化任务数据
     * @param task
     * @return
     */
    Boolean saveTask(Task task);

    /**
     * 读取某一时间点的指定分区的所有任务
     * @param date
     * @param partition
     * @return
     */
    Task getTask(Date date, Integer partition);

    /**
     * 持久化已执行任务数据
     * @param task
     * @return
     */
    Boolean saveFinishTask(Task task);

    /**
     * 读取某一时间点的任务分区数量
     * @param date
     * @return
     */
    Integer getPartitionCount(Date date);

}
