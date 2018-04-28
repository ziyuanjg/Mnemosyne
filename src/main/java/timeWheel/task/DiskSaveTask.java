package timeWheel.task;

import cn.hutool.core.date.DateUtil;
import config.ConfigParamter;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.Date;
import java.util.List;
import timeWheel.exception.FileException;
import timeWheel.exception.TaskException;

/**
 * 磁盘方式持久化任务
 * Created by 希罗 on 2018/4/28
 */
public class DiskSaveTask implements SaveTask {


    /**
     * 单次获取文件锁的最高尝试次数，避免出现一直获取不到锁，导致线程一直被持有
     */
    private final Integer getLockMaxCount = 50;

    @Override
    public Boolean saveTask(Task task) {

        String fileName = DateUtil.formatDateTime(task.getExcuteTime()) + ".txt";

        Boolean lock = Boolean.FALSE;
        Integer getLockCount = 0;

        // 获取文件锁
        do {
            try {
                Thread.sleep(50);
                lock = DiskHandler.getFileLock(fileName);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }finally {
                getLockCount++;
                if(getLockCount > getLockCount && !lock){
                    throw new FileException(TaskException.FILE_GET_LOCK_FAIL);
                }
            }
        }while (!lock);


        File file = new File(ConfigParamter.getFilePath() + fileName);

        if(!file.exists()){
            try {
                file.createNewFile();
            } catch (IOException e) {
                throw new FileException(TaskException.FILE_CREATE_FAIL, e);
            }
        }

        try (RandomAccessFile rf = new RandomAccessFile(file, "rw"); FileChannel fileChannel = rf.getChannel();){
            rf.seek(rf.length());
            rf.write(task.toString().getBytes());
            rf.write("\n".getBytes());
        }catch (IOException e){
            throw new FileException(TaskException.FILE_WRITE_FAIL, e);
        }finally {
            DiskHandler.releaseFileLock(fileName);
        }

        return Boolean.TRUE;
    }

    @Override
    public Task getTask(Date date) {


        return null;
    }

    @Override
    public Boolean saveFinishTask(Task task) {
        return null;
    }
}
