package task;

import java.util.Date;

/**
 * 1、小文件分片存储 2、大文件分秒存储 3、redis 4、mysql Created by Mr.Luo on 2018/4/28
 */
public interface TaskHandler {

    /**
     * 持久化任务数据
     */
    Boolean saveTask(Task task);

    /**
     * 读取某一时间点的指定分区的所有任务
     */
    Task getTask(Date date, Integer partition);

    /**
     * 读取某一时间点的任务分区数量
     */
    Integer getPartitionCount(Date date);

}
