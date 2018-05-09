package task;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import java.io.FilenameFilter;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.util.Date;
import task.exception.FileException;
import task.exception.TaskExceptionEnum;

/**
 * 磁盘方式持久化任务
 * Created by 希罗 on 2018/4/28
 */
public class DiskTaskHandler extends AbstractTaskHandler {

    /**
     * 单次获取文件锁的最高尝试次数，避免出现一直获取不到锁，导致线程一直被持有
     */
    private final Integer getLockMaxCount = 50;

    private final String FINISH_SUFFIX = "_finished";

    @Override
    Boolean save(Task task) {

        if(task == null){
            return Boolean.FALSE;
        }

        DateTime excuteTime = DateUtil.date(task.getExcuteTime());
        String taskDate = DateUtil.formatDateTime(task.getExcuteTime());

        File file;
        Integer fileNum = 0;
        String fileName;

        do {
            fileName = taskDate + "." + fileNum++;
            String filePath = getFilePath(excuteTime);
            file = new File(filePath + fileName);

        }while(file.exists() && file.length() > 1024 * 1024 * 5);

        getFileLock(fileName);

        getTaskFromFile(task, fileName, file);

        return Boolean.TRUE;
    }

    private String getFilePath(DateTime excuteTime) {
        return SaveConfig.getFilePath()
                + excuteTime.year() + "/"
                + excuteTime.month() + "/"
                + excuteTime.dayOfMonth() + "/"
                + excuteTime.hour(true) + "/"
                + excuteTime.minute() + "/"
                + excuteTime.second() + "/";
    }

    private void getTaskFromFile(Task task, String fileName, File file) {
        if(!file.getParentFile().exists()){
            try {
                file.getParentFile().mkdirs();
                if(!file.exists()){
                    file.createNewFile();
                }
            } catch (Exception e) {
                throw new FileException(TaskExceptionEnum.FILE_CREATE_FAIL, e);
            }
        }

        try (RandomAccessFile rf = new RandomAccessFile(file, "rw")){
            rf.seek(file.length());
            rf.write(JSON.toJSONString(task).getBytes());
            rf.write("\n".getBytes());
        }catch (IOException e){
            throw new FileException(TaskExceptionEnum.FILE_WRITE_FAIL, e);
        }finally {
            FileHandler.releaseFileLock(fileName);
        }
    }

    @Override
    Task get(Date date, Integer partition) {

        if(date == null){
            throw new FileException(TaskExceptionEnum.PARAM_ERROR_DATE);
        }

        if(partition == null){
            throw new FileException(TaskExceptionEnum.PARAM_ERROR_PARTITION);
        }

        String fileName = DateUtil.formatDateTime(date) + "." + partition;

        getFileLock(fileName);

        DateTime excuteTime = DateUtil.date(date);
        String filePath = getFilePath(excuteTime);
        File file = new File(filePath + fileName);

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
            throw new FileException(TaskExceptionEnum.FILE_WRITE_FAIL, e);
        }finally {
            FileHandler.releaseFileLock(fileName);
        }

        return task;
    }

    @Override
    Boolean saveFinish(Task task) {

        if(task == null){
            return Boolean.FALSE;
        }

        String fileName = DateUtil.formatDateTime(task.getExcuteTime()) + FINISH_SUFFIX;

        getFileLock(fileName);

        DateTime excuteTime = DateUtil.date(task.getExcuteTime());
        String filePath = getFilePath(excuteTime);
        File file = new File(filePath + fileName);

        getTaskFromFile(task, fileName, file);

        return Boolean.TRUE;
    }

    @Override
    Integer getPartitionNum(Date date) {

        DateTime excuteTime = DateUtil.date(date);
        String filePath = getFilePath(excuteTime);

        File file = new File(filePath);

        if(file.isDirectory()){

            return file.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return !name.contains(FINISH_SUFFIX);
                }
            }).length;
        }else{
            return 0;
        }
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
                    throw new FileException(TaskExceptionEnum.FILE_GET_LOCK_FAIL);
                }
            }
        }
    }
}
