package slave;

import common.BizResult;
import common.Configuration;
import java.util.Date;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import slave.exception.SlaveException;
import slave.exception.SlaveExceptionEnum;
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
    public BizResult execute(@HeaderParam("date") Date date, @HeaderParam("partition") Integer partition){

        TaskHandler taskHandler = Configuration.getTaskHandler();

        Task task = taskHandler.getTask(date, partition);

        while(task != null){
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
     * @param task
     */
    @Path("execute")
    @POST
    public BizResult execute(Task task){

        try {
            Configuration.getExecuteTaskThreadPool().addTask(task);
        } catch (Exception e) {
            throw new SlaveException(SlaveExceptionEnum.EXECUTE_ERROR);
        }

        return BizResult.createSuccessResult(null);
    }

    /**
     * 新增任务
     * @param task
     */
    @Path("add")
    @POST
    public BizResult add(Task task){

        TaskHandler taskHandler = Configuration.getTaskHandler();
        taskHandler.saveTask(task);
        return BizResult.createSuccessResult(null);
    }


}
