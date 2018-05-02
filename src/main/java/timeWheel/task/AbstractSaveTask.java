package timeWheel.task;

import config.SaveConfig;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by 希罗 on 2018/5/2
 */
public abstract class AbstractSaveTask implements SaveTask {


    ThreadPoolExecutor pool = new ThreadPoolExecutor(SaveConfig.getCorePoolSize(), SaveConfig.getMaxPoolSize(), SaveConfig.getKeepAliveTime(), TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());

    @Override
    public Boolean saveTask(Task task) {

        try {
            Future<Boolean> future = pool.submit(() -> save(task));
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
            Future<Task> future = pool.submit(() -> get(date, partition));
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
            Future<Boolean> future = pool.submit(() -> saveFinishTask(task));
            return future.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return Boolean.FALSE;
        } catch (ExecutionException e) {
            e.printStackTrace();
            return Boolean.FALSE;
        }
    }

    public abstract Boolean save(Task task);

    public abstract Task get(Date date, Integer partition);

    public abstract Boolean saveFinish(Task task);
}
