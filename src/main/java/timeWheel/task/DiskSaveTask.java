package timeWheel.task;

import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import config.SaveConfig;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.util.Date;
import timeWheel.exception.FileException;
import timeWheel.exception.TaskException;

/**
 * 磁盘方式持久化任务
 * Created by 希罗 on 2018/4/28
 */
public class DiskSaveTask extends AbstractSaveTask {


    /**
     * 单次获取文件锁的最高尝试次数，避免出现一直获取不到锁，导致线程一直被持有
     */
    private final Integer getLockMaxCount = 50;

    @Override
    public Boolean save(Task task) {

        if(task == null){
            return Boolean.FALSE;
        }

        String taskDate = DateUtil.formatDateTime(task.getExcuteTime());

        File file;
        Integer fileNum = 0;
        String fileName;

        do {
            fileName = taskDate + "." + fileNum++;
            file = new File(SaveConfig.getFilePath() + fileName);

        }while(file.exists() && file.length() > 1024 * 1024 * 5);

        getFileLock(fileName);

        getTaskFromFile(task, fileName, file);

        return Boolean.TRUE;
    }

    private void getTaskFromFile(Task task, String fileName, File file) {
        if(!file.exists()){
            try {
                file.createNewFile();
            } catch (IOException e) {
                throw new FileException(TaskException.FILE_CREATE_FAIL, e);
            }
        }

        try (RandomAccessFile rf = new RandomAccessFile(file, "rw")){
            rf.seek(file.length());
            rf.write(JSON.toJSONString(task).getBytes());
            rf.write("\n".getBytes());
        }catch (IOException e){
            throw new FileException(TaskException.FILE_WRITE_FAIL, e);
        }finally {
            FileHandler.releaseFileLock(fileName);
        }
    }

    @Override
    public Task get(Date date, Integer partition) {

        if(date == null){
            throw new FileException(TaskException.PARAM_ERROR_DATE);
        }

        if(partition == null){
            throw new FileException(TaskException.PARAM_ERROR_PARTITION);
        }

        String fileName = DateUtil.formatDateTime(date) + "." + partition;

        getFileLock(fileName);

        File file = new File(SaveConfig.getFilePath() + fileName);

        if(!file.exists()){
           return null;
        }

        Task task = null;

        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)))){
            String taskString;
            while((taskString = br.readLine()) != null){
                Task lastTask = JSONObject.parseObject(taskString, Task.class);
                if(task == null){
                    task = lastTask;
                }else{
                    task.setLastTask(lastTask);
                    lastTask.setBeforeTask(task);
                    task = lastTask;
                }
            }
        }catch (IOException e){
            throw new FileException(TaskException.FILE_WRITE_FAIL, e);
        }finally {
            FileHandler.releaseFileLock(fileName);
        }

        return task;
    }

    @Override
    public Boolean saveFinish(Task task) {

        if(task == null){
            return Boolean.FALSE;
        }

        String fileName = DateUtil.formatDateTime(task.getExcuteTime()) + "_finished";

        getFileLock(fileName);

        File file = new File(SaveConfig.getFilePath() + fileName);

        getTaskFromFile(task, fileName, file);

        return Boolean.TRUE;
    }


    private void getFileLock(String fileName) {
        Integer getLockCount = 0;

        while (!FileHandler.getFileLock(fileName)){
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                getLockCount++;
                if(getLockCount > getLockMaxCount){
                    FileHandler.releaseFileLock(fileName);
                    throw new FileException(TaskException.FILE_GET_LOCK_FAIL);
                }
            }
        }
    }
}
