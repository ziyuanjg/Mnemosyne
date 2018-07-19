package com.mnemosyne.task.disk;

import cn.hutool.core.date.DateTime;
import com.alibaba.fastjson.JSON;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import com.mnemosyne.task.SaveConfig;
import com.mnemosyne.task.Task;
import com.mnemosyne.task.exception.FileException;
import com.mnemosyne.task.exception.TaskExceptionEnum;

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
    private static final Integer GET_LOCK_MAX_COUNT = 50;

    /**
     * 每个主索引的长度
     */
    private static final Integer MAIN_INDEX_LENGTH = 128;

    /**
     * 主索引配置信息长度
     */
    private static final Integer MAIN_INDEX_CONFIG_LENGTH = 1024;

    /**
     * 主配置文件
     */
    private static final File MAIN_CONFIG_FILE = new File(SaveConfig.getFilePath() + "/main_config");

    /**
     * 主索引文件
     */
    private static final File MAIN_INDEX_FILE = new File(SaveConfig.getFilePath() + "/main_index");


    void FileUtil(){

        if (!MAIN_CONFIG_FILE.getParentFile().exists()) {
            try {
                MAIN_CONFIG_FILE.getParentFile().mkdirs();
                if (!MAIN_CONFIG_FILE.exists()) {
                    MAIN_CONFIG_FILE.createNewFile();
                }
            } catch (Exception e) {
                throw new FileException(TaskExceptionEnum.FILE_MAINCONFIG_WRITE_ERROR, e);
            }
        }

        if(!MAIN_INDEX_FILE.getParentFile().exists()){
            try {
                MAIN_INDEX_FILE.getParentFile().mkdirs();
                if(!MAIN_INDEX_FILE.exists()){
                    MAIN_INDEX_FILE.createNewFile();
                }
            } catch (IOException e) {
                throw new FileException(TaskExceptionEnum.FILE_MAINCONFIG_WRITE_ERROR, e);
            }
        }
    }

    /**
     * 设置主配置
     */
    public void setMainConfig(MainConfig mainConfig) {

        try {
            getFileLock(MAIN_CONFIG_FILE.getName());
            byte[] bytes = JSON.toJSONBytes(mainConfig);
            writeToFile(MAIN_CONFIG_FILE, bytes, bytes.length, 0L);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            releaseFileLock(MAIN_CONFIG_FILE.getName());
        }
    }

    public MainConfig getMainConfig() {

        if (!MAIN_CONFIG_FILE.exists()) {
            MainConfig mainConfig = MainConfig.builder().taskCount(new AtomicLong(0)).finishedLastDate(null).build();
            setMainConfig(mainConfig);
            return mainConfig;
        }

        try (RandomAccessFile rf = new RandomAccessFile(MAIN_CONFIG_FILE, "rw")) {

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
    public void SaveTaskToFile(Task task, File file, Long startIndex) {

        if (!file.getParentFile().exists()) {
            try {
                file.getParentFile().mkdirs();
                if (!file.exists()) {
                    file.createNewFile();
                    setFileConfig(file);
                }
            } catch (Exception e) {
                throw new FileException(TaskExceptionEnum.FILE_CREATE_FAIL, e);
            }
        }

        try {
            getFileLock(file.getName());
            startIndex = writeToFile(file, JSON.toJSONBytes(task), SaveConfig.getTaskMAXLength(), startIndex);
            addMainIndex(task.getId(), task.getExcuteTime(), startIndex, null);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            releaseFileLock(file.getName());
        }
    }

    /**
     * 获取文件头的配置信息
     */
    public FileConfig getFileConfig(File file) {

        try (RandomAccessFile rf = new RandomAccessFile(file, "r")) {
            return getFileConfig(rf);
        } catch (IOException e) {
            e.printStackTrace();
            throw new FileException(TaskExceptionEnum.FILE_GET_CONFIG_FAIL, e);
        }
    }

    /**
     * 获取文件头的配置信息
     */
    private FileConfig getFileConfig(RandomAccessFile rf) {

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

            for (Integer nextId = 0; nextId < fileConfig.getEndTaskId(); nextId++) {

                Long seekIndex = FILE_CONFIG_LENGTH.longValue() + (SaveConfig.getTaskMAXLength() * nextId);
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

    /**
     * 保存文件头信息
     */
    public void setFileConfig(File file, FileConfig fileConfig) {

        byte[] configBytes = JSON.toJSONBytes(fileConfig);
        writeToFile(file, configBytes, FILE_CONFIG_LENGTH, 0L);
    }

    /**
     * 新增主索引
     */
    public void addMainIndex(Long id, Date excuteTime, Long fileIndex, MainIndexConfig mainIndexConfig){

        getFileLock(MAIN_INDEX_FILE.getName());

        // 更新主索引配置信息
        if(mainIndexConfig == null){
            mainIndexConfig = getMainIndexConfig();
        }
        Long endId = mainIndexConfig.addTask();

        setMainIndexConfig(mainIndexConfig);

        Long startIndex = MAIN_INDEX_CONFIG_LENGTH + (MAIN_INDEX_LENGTH * (endId - 1));

        MainIndex mainIndex = MainIndex.builder().Id(id).excuteTime(excuteTime).fileIndex(fileIndex).build();
        writeToFile(MAIN_INDEX_FILE, JSON.toJSONBytes(mainIndex), MAIN_INDEX_LENGTH, startIndex);
    }

    /**
     * 根据索引获取任务地址
     */
    public MainIndex getMainIndex(Long id){

        try(RandomAccessFile rf = new RandomAccessFile(MAIN_INDEX_FILE, "r")) {

            byte[] bytes = new byte[MAIN_INDEX_LENGTH];

            Long startIndex = MAIN_INDEX_CONFIG_LENGTH + (MAIN_INDEX_LENGTH * (id - 1));
            rf.seek(startIndex);
            rf.read(bytes);
            return JSON.parseObject(bytes, MainIndex.class);
        } catch (Exception e) {
            e.printStackTrace();
            throw new FileException(TaskExceptionEnum.FILE_GET_MAIN_INDEX_ERROR, e);
        }

    }

    /**
     * 获取主索引配置信息
     */
    public MainIndexConfig getMainIndexConfig(){

        try(RandomAccessFile rf = new RandomAccessFile(MAIN_INDEX_FILE, "r")) {
            byte[] bytes = new byte[MAIN_INDEX_CONFIG_LENGTH];
            rf.read(bytes);
            MainIndexConfig mainIndexConfig = JSON.parseObject(bytes, MainIndexConfig.class);
            if(mainIndexConfig == null){
                mainIndexConfig = MainIndexConfig.builder().endId(new AtomicLong(0)).taskNum(new AtomicLong(0)).build();
            }
            return mainIndexConfig;
        } catch (Exception e) {
            e.printStackTrace();
            throw new FileException(TaskExceptionEnum.FILE_GET_MAIN_INDEX_CONFIG_ERROR, e);
        }
    }

    /**
     * 更新主索引配置信息
     */
    public void setMainIndexConfig(MainIndexConfig mainIndexConfig){

        try {
            getFileLock(MAIN_INDEX_FILE.getName());
            byte[] configBytes = JSON.toJSONBytes(mainIndexConfig);
            writeToFile(MAIN_INDEX_FILE, configBytes, MAIN_INDEX_CONFIG_LENGTH, 0L);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            releaseFileLock(MAIN_INDEX_FILE.getName());
        }
    }

    private void setFileConfig(File file) {

        FileConfig fileConfig = FileConfig.builder()
                .taskMAXLength(SaveConfig.getTaskMAXLength())
                .finishedTaskNum(new AtomicInteger(0))
                .taskNum(new AtomicInteger(0))
                .build();

        setFileConfig(file, fileConfig);
    }

    private Long writeToFile(File file, byte[] value, Integer length, Long startIndex) {

        if (length == null || value == null || value.length == 0 || length < value.length) {
            return null;
        }

        startIndex = startIndex == null ? file.length() : startIndex;
        try (RandomAccessFile rf = new RandomAccessFile(file, "rw")) {

            rf.seek(startIndex);

            if (length > value.length) {
                rf.write(new byte[length - value.length]);
            }
        } catch (Exception e) {
            throw new FileException(TaskExceptionEnum.FILE_CREATE_FAIL, e);
        }
        return startIndex;
    }
}
