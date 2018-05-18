package task;

import cn.hutool.core.date.DateTime;
import com.alibaba.fastjson.JSON;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
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
    public void SaveTaskToFile(Task task, String fileName, File file, Integer fileNum, Long startIndex) {
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

        writeToFile(file, fileName, JSON.toJSONBytes(task), SaveConfig.getTaskMAXLength(), startIndex);
    }

    /**
     * 获取文件头的配置信息
     */
    public FileConfig getFileConfig(File file) {

        try {
            RandomAccessFile rf = new RandomAccessFile(file, "r");
            return getFileConfig(rf);
        } catch (FileNotFoundException e) {
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

            for (Integer nextId = fileConfig.getStartId(); nextId < fileConfig.getEndId(); nextId++) {

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

    private void setFileConfig(File file, Integer fileNum) {

        FileConfig fileConfig = FileConfig.builder()
                .taskMAXLength(SaveConfig.getTaskMAXLength())
                .finishedTask(0)
                .startId((fileNum - 1) * 1000)
                .build();
        byte[] configBytes = JSON.toJSONBytes(fileConfig);
        writeToFile(file, null, configBytes, FILE_CONFIG_LENGTH, null);
    }

    private Integer writeToFile(File file, String fileName, byte[] value) {
        return writeToFile(file, fileName, value, value.length, null);
    }

    private Integer writeToFile(File file, String fileName, byte[] value, Integer length, Long startIndex) {

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
        } finally {
            if (fileName != null) {
                releaseFileLock(fileName);
            }
        }
        return length;
    }
}
