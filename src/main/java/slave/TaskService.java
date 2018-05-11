package slave;

import common.Configuration;
import java.util.Date;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import task.Task;
import task.TaskHandler;

/**
 * 任务处理器
 * Created by Mr.Luo on 2018/5/3
 */
@Path("/salve")
@Produces({"application/xml", "application/json"})
public class TaskService {

    /**
     * 执行指定时间区任务
     * @param date
     */
    @Path("execute")
    @GET
    public void execute(@HeaderParam("date") Date date, @HeaderParam("partition") Integer partition){

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
    @Path("execute")
    @POST
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
    @Path("add")
    @POST
    public void add(Task task){

        TaskHandler taskHandler = Configuration.getTaskHandler();
        taskHandler.saveTask(task);
    }


}
