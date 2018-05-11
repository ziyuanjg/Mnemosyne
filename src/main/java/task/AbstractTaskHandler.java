package task;

import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by Mr.Luo on 2018/5/2
 */
public abstract class AbstractTaskHandler implements TaskHandler {

    ThreadPoolExecutor savePool = new ThreadPoolExecutor(SaveConfig.getCorePoolSize(), SaveConfig.getMaxPoolSize(), SaveConfig.getKeepAliveTime(), TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
    ThreadPoolExecutor getPool = new ThreadPoolExecutor(SaveConfig.getCorePoolSize(), SaveConfig.getMaxPoolSize(), SaveConfig.getKeepAliveTime(), TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());

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
    public Boolean saveFinishTask(Task task) {

        try {
            Future<Boolean> future = savePool.submit(() -> saveFinish(task));
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
    public Integer getPartitionCount(Date date) {
        return null;
    }

    abstract Integer getPartitionNum(Date date);

    abstract Boolean save(Task task);

    abstract Task get(Date date, Integer partition);

    abstract Boolean saveFinish(Task task);
}
