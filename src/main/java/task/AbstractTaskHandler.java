package task;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import task.SaveConfig;
import task.Task;
import task.TaskHandler;

/**
 * Created by Mr.Luo on 2018/5/2
 */
public abstract class AbstractTaskHandler implements TaskHandler {

    ThreadPoolExecutor savePool = new ThreadPoolExecutor(SaveConfig.getCorePoolSize(), SaveConfig.getMaxPoolSize(),
            SaveConfig.getKeepAliveTime(), TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
    ThreadPoolExecutor getPool = new ThreadPoolExecutor(SaveConfig.getCorePoolSize(), SaveConfig.getMaxPoolSize(),
            SaveConfig.getKeepAliveTime(), TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());

    @Override
    public Boolean saveTask(Task task) {

        try {
            Future<Boolean> future = savePool.submit(() -> save(task));
            return future.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return Boolean.FALSE;
        } catch (ExecutionException e) {
            e.printStackTrace();
            return Boolean.FALSE;
        }
    }

    @Override
    public Task getTask(Date date, Integer partition) {

        try {
            Future<Task> future = getPool.submit(() -> get(date, partition));
            return future.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        } catch (ExecutionException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Integer getPartitionCount(Date date) {

        return getPartitionNum(date);
    }

    @Override
    public List<Task> getUnFinishedTaskIdList(Date date) {

        return getUnFinishedTaskIds(date);
    }

    protected abstract List<Task> getUnFinishedTaskIds(Date date);

    protected abstract Integer getPartitionNum(Date date);

    protected abstract Boolean save(Task task);

    protected abstract Task get(Date date, Integer partition);

}
