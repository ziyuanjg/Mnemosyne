package com.mnemosyne.slave;

import com.mnemosyne.common.BizResult;
import com.mnemosyne.common.Configuration;
import com.mnemosyne.slave.exception.SlaveException;
import com.mnemosyne.slave.exception.SlaveExceptionEnum;
import com.mnemosyne.task.Task;
import com.mnemosyne.task.TaskHandler;
import java.util.Date;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

/**
 * 任务处理器 Created by Mr.Luo on 2018/5/3
 */
@Path("salve")
@Produces({"application/json"})
public class TaskService {

    /**
     * 执行指定时间区任务
     */
    @Path("executeByPartition")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public BizResult execute(@FormParam("date") Date date, @FormParam("partition") Integer partition) {

        TaskHandler taskHandler = Configuration.getTaskHandler();

        Task task = taskHandler.getTask(date, partition);

        while (task != null) {
            try {
                Configuration.getExecuteTaskThreadPool().addTask(task);
            } catch (Exception e) {
                e.printStackTrace();
            }
            task = task.getBeforeTask();
        }

        return BizResult.createSuccessResult(null);
    }

    /**
     * 执行指定任务
     */
    @Path("executeByTask")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public BizResult execute(Task task) {

        try {
            Configuration.getExecuteTaskThreadPool().addTask(task);
        } catch (Exception e) {
            throw new SlaveException(SlaveExceptionEnum.EXECUTE_ERROR);
        }

        return BizResult.createSuccessResult(null);
    }

    /**
     * 新增任务
     */
    @Path("add")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public BizResult add(Task task) {

        TaskHandler taskHandler = Configuration.getTaskHandler();
        taskHandler.saveTask(task);
        return BizResult.createSuccessResult(null);
    }

    /**
     * 查询此时间点之前的所有未完成任务id列表
     */
    @Path("getUnFinishedTaskIdList")
    @GET
    public BizResult getUnFinishedTaskIdList(@QueryParam("date") Date date) {

        TaskHandler taskHandler = Configuration.getTaskHandler();
        List<Task> idList = taskHandler.getUnFinishedTaskIdList(date);
        return BizResult.createSuccessResult(idList);
    }
}
