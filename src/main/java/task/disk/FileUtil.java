package task.disk;

import cn.hutool.core.date.DateTime;
import com.alibaba.fastjson.JSON;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import task.SaveConfig;
import task.Task;
import task.exception.FileException;
import task.exception.TaskExceptionEnum;

/**
 * Created by Mr.Luo on 2018/5/16
 */
public class FileUtil {

    /**
     * 每个数据文件的头信息长度
     */
    public final static Integer FILE_CONFIG_LENGTH = 1024;

    /**
     * 单次获取文件锁的最高尝试次数，避免出现一直获取不到锁，导致线程一直被持有
     */
    private final Integer GET_LOCK_MAX_COUNT = 50;

    /**
     * 主配置文件
     */
    private final File MAIN_CONFIG_FILE = new File(SaveConfig.getFilePath() + "/main_config");

    /**
     * 设置主配置
     * @param mainConfig
     */
    public void setMainConfig(MainConfig mainConfig){

        try {
            if(!MAIN_CONFIG_FILE.exists()){
                MAIN_CONFIG_FILE.createNewFile();
            }

            byte[] bytes = JSON.toJSONBytes(mainConfig);
            writeToFile(MAIN_CONFIG_FILE, bytes, bytes.length, 0L);

        } catch (IOException e) {
            e.printStackTrace();
            throw new FileException(TaskExceptionEnum.FILE_MAINCONFIG_WRITE_ERROR, e);
        }
    }

    public MainConfig getMainConfig(){

        if(!MAIN_CONFIG_FILE.exists()){
            MainConfig mainConfig = MainConfig.builder().taskCount(new AtomicLong(0)).finishedLastDate(null).build();
            setMainConfig(mainConfig);
            return mainConfig;
        }

        try (RandomAccessFile rf = new RandomAccessFile(MAIN_CONFIG_FILE, "rw")){

            Long length = MAIN_CONFIG_FILE.length();
            byte[] bytes = new byte[length.intValue()];
            rf.read(bytes);

            return JSON.parseObject(bytes, MainConfig.class);
        } catch (IOException e) {
            e.printStackTrace();
            throw new FileException(TaskExceptionEnum.FILE_MAINCONFIG_READ_ERROR, e);
        }

    }

    /**
     * 拼装文件路径
     */
    public String getFilePath(DateTime excuteTime) {
        return SaveConfig.getFilePath()
                + excuteTime.year() + "/"
                + excuteTime.month() + "/"
                + excuteTime.dayOfMonth() + "/"
                + excuteTime.hour(true) + "/"
                + excuteTime.minute() + "/"
                + excuteTime.second() + "/";
    }

    /**
     * 获取文件锁
     */
    public void getFileLock(String fileName) {
        Integer getLockCount = 0;

        while (!FileLockHandler.getFileLock(fileName)) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                getLockCount++;
                if (getLockCount > GET_LOCK_MAX_COUNT) {
                    releaseFileLock(fileName);
                    throw new FileException(TaskExceptionEnum.FILE_GET_LOCK_FAIL);
                }
            }
        }
    }

    public void releaseFileLock(String fileName) {
        FileLockHandler.releaseFileLock(fileName);
    }


    /**
     * 将任务持久化到磁盘文件
     */
    public void SaveTaskToFile(Task task, File file, Integer fileNum, Long startIndex) {
        if (!file.getParentFile().exists()) {
            try {
                file.getParentFile().mkdirs();
                if (!file.exists()) {
                    file.createNewFile();
                    setFileConfig(file, fileNum);
                }
            } catch (Exception e) {
                throw new FileException(TaskExceptionEnum.FILE_CREATE_FAIL, e);
            }
        }

        writeToFile(file, JSON.toJSONBytes(task), SaveConfig.getTaskMAXLength(), startIndex);
    }

    /**
     * 获取文件头的配置信息
     */
    public FileConfig getFileConfig(File file) {

        try (RandomAccessFile rf = new RandomAccessFile(file, "r")){
            return getFileConfig(rf);
        } catch (IOException e){
            e.printStackTrace();
            throw new FileException(TaskExceptionEnum.FILE_GET_CONFIG_FAIL, e);
        }
    }

    /**
     * 获取文件头的配置信息
     */
    public FileConfig getFileConfig(RandomAccessFile rf) {

        try {
            byte[] bytes = new byte[FILE_CONFIG_LENGTH];
            rf.read(bytes);
            return JSON.parseObject(bytes, FileConfig.class);
        } catch (Exception e) {
            e.printStackTrace();
            throw new FileException(TaskExceptionEnum.FILE_GET_CONFIG_FAIL, e);
        }
    }

    /**
     * 读取任务链
     */
    public Task getTaskFromFile(File file) {

        try (RandomAccessFile rf = new RandomAccessFile(file, "r")) {

            FileConfig fileConfig = getFileConfig(rf);

            rf.skipBytes(FILE_CONFIG_LENGTH);

            byte[] taskBytes = new byte[fileConfig.getTaskMAXLength()];

            Task task = null;

            for (Integer nextId = fileConfig.getStartId(); nextId < fileConfig.getEndId().get(); nextId++) {

                Long seekIndex = FILE_CONFIG_LENGTH.longValue() + (SaveConfig.getTaskMAXLength() * (nextId - fileConfig
                        .getStartId()));
                rf.seek(seekIndex);
                rf.read(taskBytes);
                Task nextTask = null;
                try {
                    nextTask = JSON.parseObject(taskBytes, Task.class);
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new FileException(TaskExceptionEnum.FILE_TASK_ERROR, e);
                }

                if (task == null) {
                    task = nextTask;
                    continue;
                }

                nextTask.setBeforeTask(task);
                task = nextTask;
            }

            return task;
        } catch (Exception e) {
            e.printStackTrace();
            throw new FileException(TaskExceptionEnum.FILE_TASK_ERROR, e);
        }
    }

    public void setFileConfig(File file, FileConfig fileConfig){

        byte[] configBytes = JSON.toJSONBytes(fileConfig);
        writeToFile(file, configBytes, FILE_CONFIG_LENGTH, 0L);
    }

    private void setFileConfig(File file, Integer fileNum) {

        FileConfig fileConfig = FileConfig.builder()
                .taskMAXLength(SaveConfig.getTaskMAXLength())
                .finishedTask(new AtomicInteger(0))
                .startId((fileNum - 1) * 1000)
                .endId(new AtomicInteger((fileNum - 1) * 1000))
                .build();

        setFileConfig(file, fileConfig);
    }

    private Integer writeToFile(File file, byte[] value, Integer length, Long startIndex) {

        if (length == null || value == null || value.length == 0 || length < value.length) {
            return 0;
        }
        try (RandomAccessFile rf = new RandomAccessFile(file, "rw")) {

            if (startIndex != null) {
                rf.seek(startIndex);
            } else {
                rf.seek(file.length());
            }
            rf.write(value);

            if (length > value.length) {
                rf.write(new byte[length - value.length]);
            }
        } catch (Exception e) {
            throw new FileException(TaskExceptionEnum.FILE_CREATE_FAIL, e);
        }
        return length;
    }
}
