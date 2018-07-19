package com.mnemosyne.task;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

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
            Future<Boolean> future = savePool.submit(() -> _save(task));
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
            Future<Task> future = getPool.submit(() -> _get(date, partition));
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

        return _getPartitionCount(date);
    }

    @Override
    public List<Task> getUnFinishedTaskIdList(Date date) {

        return _getUnFinishedTaskIdList(date);
    }

    @Override
    public Task getTaskById(Long id) {

        return _getTaskById(id);
    }

    @Override
    public Long getNewTaskId() {

        return _getNewTaskId();
    }

    protected abstract List<Task> _getUnFinishedTaskIdList(Date date);

    protected abstract Integer _getPartitionCount(Date date);

    protected abstract Boolean _save(Task task);

    protected abstract Task _get(Date date, Integer partition);

    protected abstract Task _getTaskById(Long id);

    protected abstract Long _getNewTaskId();

}
