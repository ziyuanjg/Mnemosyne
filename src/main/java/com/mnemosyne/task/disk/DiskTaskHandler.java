package com.mnemosyne.task.disk;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import com.mnemosyne.task.AbstractTaskHandler;
import com.mnemosyne.task.SaveConfig;
import com.mnemosyne.task.Task;
import com.mnemosyne.task.exception.FileException;
import com.mnemosyne.task.exception.TaskExceptionEnum;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 小文件分片持久化任务 Created by Mr.Luo on 2018/4/28
 */
public class DiskTaskHandler extends AbstractTaskHandler {

    private final FileUtil fileUtil = new FileUtil();

    private final Integer FILE_MAX_LENGTH =
            (SaveConfig.getTaskMAXLength() * SaveConfig.getTaskNumOfPartition()) + FileUtil.FILE_CONFIG_LENGTH;

    private final MainConfig mainConfig = fileUtil.getMainConfig();

    private final MainIndexConfig mainIndexConfig = fileUtil.getMainIndexConfig();

    @Override
    protected Boolean _save(Task task) {

        if (task == null) {
            return Boolean.FALSE;
        }

        if (task.isFinish()) {
            saveFinishedTask(task);
        } else {
            saveUnfinishedTask(task);
        }

        return Boolean.TRUE;
    }

    /**
     * 持久化新增任务
     * @param task
     */
    private void saveUnfinishedTask(Task task) {
        DateTime excuteTime = DateUtil.date(task.getExcuteTime());
        String taskDate = DateUtil.formatDateTime(task.getExcuteTime());

        File file;
        Integer fileNum = 0;
        String fileName;

        do {
            fileName = taskDate + "." + fileNum++;
            String filePath = fileUtil.getFilePath(excuteTime);
            file = new File(filePath + fileName);

        } while (file.exists() && file.length() > FILE_MAX_LENGTH);

        fileUtil.SaveTaskToFile(task, file,null, fileNum, Boolean.TRUE);

        FileConfig fileConfig = fileUtil.getFileConfig(file);
        fileConfig.addTaskNum();

        fileUtil.setFileConfig(file, fileConfig);

        mainConfig.getTaskCount().incrementAndGet();
        fileUtil.setMainConfig(mainConfig);
    }

    /**
     * 持久化已完成任务
     * @param task
     */
    private void saveFinishedTask(Task task) {

        // 先根据索引获取任务在文件中的位置
        MainIndex mainIndex = fileUtil.getMainIndex(task.getId());

        DateTime excuteTime = DateUtil.date(mainIndex.getExcuteTime());
        String taskDate = DateUtil.formatDateTime(mainIndex.getExcuteTime());
        String fileName = taskDate + "." + mainIndex.getPartation();
        File file = new File(fileUtil.getFilePath(excuteTime) + fileName);

        FileConfig fileConfig = fileUtil.getFileConfig(file);

        Long startIndex = mainIndex.getFileIndex();

        fileUtil.SaveTaskToFile(task, file, startIndex, null, Boolean.FALSE);

        fileConfig.addFinishedTaskNum();
        fileUtil.setFileConfig(file, fileConfig);

        // 此分片全部任务都已经执行完毕,刷新主体中的最后全部执行完毕的分片
        if(fileConfig.getFinishedTaskNum().get() == fileConfig.getTaskNum().get()){
            if(mainConfig == null || mainConfig.getFinishedAllTaskLastDate().getTime() < excuteTime.getTime()){
                mainConfig.setFinishedAllTaskLastDate(excuteTime);
            }
        }

        // 更新主体信息
        finishTaskNote(task);

        fileUtil.setMainConfig(mainConfig);
    }

    @Override
    protected Task _get(Date date, Integer partition) {

        if (date == null) {
            throw new FileException(TaskExceptionEnum.PARAM_ERROR_DATE);
        }

        if (partition == null) {
            throw new FileException(TaskExceptionEnum.PARAM_ERROR_PARTITION);
        }

        String fileName = DateUtil.formatDateTime(date) + "." + partition;

        try {

            fileUtil.getFileLock(fileName);
            DateTime excuteTime = DateUtil.date(date);
            String filePath = fileUtil.getFilePath(excuteTime);
            File file = new File(filePath + fileName);

            if (!file.exists()) {
                fileUtil.releaseFileLock(fileName);
                return null;
            }

            Task task = fileUtil.getTaskFromFile(file);

            return task;
        } catch (Exception e) {
            e.printStackTrace();
            throw new FileException(TaskExceptionEnum.FILE_TASK_ERROR, e);
        } finally {
            fileUtil.releaseFileLock(fileName);
        }
    }

    @Override
    protected Integer _getPartitionCount(Date date) {

        DateTime excuteTime = DateUtil.date(date);
        String filePath = fileUtil.getFilePath(excuteTime);

        File file = new File(filePath);

        if (file.isDirectory()) {
            return file.listFiles().length;
        } else {
            return 0;
        }
    }

    @Override
    protected List<Task> _getUnFinishedTaskIdList(Date date) {

        Date lastRunDate = mainConfig.getFinishedLastDate();

        if(lastRunDate != null && lastRunDate.getTime() >= date.getTime()){
            return new ArrayList<Task>(0);
        }

        DateTime dateTime = DateUtil.date(lastRunDate);

        List<Task> taskList = new ArrayList<>();
        do {

            File datePath = new File(fileUtil.getFilePath(dateTime));
            if(datePath.exists() && datePath.isDirectory()){
                File[] files = datePath.listFiles();
                Arrays.stream(files).forEach(file -> {
                    FileConfig fileConfig = fileUtil.getFileConfig(file);
                    if(fileConfig == null || fileConfig.isFinish()){
                        return;
                    }

                    Task task = fileUtil.getTaskFromFile(file);
                    if(task != null){
                        do {

                            if(!task.isFinish()){
                                taskList.add(task);
                            }
                        } while((task = task.getBeforeTask()) != null);
                    }
                });
            }

            dateTime.setField(DateField.SECOND, dateTime.second() + 1);
        } while (dateTime.getTime() < lastRunDate.getTime());

        return taskList;
    }

    @Override
    protected Task _getTaskById(Long id) {

        MainIndex mainIndex = fileUtil.getMainIndex(id);
        if(mainIndex == null){
            return null;
        }

        DateTime excuteTime = DateUtil.date(mainIndex.getExcuteTime());
        String taskDate = DateUtil.formatDateTime(mainIndex.getExcuteTime());
        String fileName = taskDate + "." + mainIndex.getPartation();
        File file = new File(fileUtil.getFilePath(excuteTime) + fileName);

        return fileUtil.getTask(file, mainIndex.getFileIndex());
    }

    @Override
    protected Long _getNewTaskId() {
        return mainIndexConfig.getEndId() + 1;
    }

    public void finishTaskNote(Task task){

        if(mainConfig.getFinishedLastDate() == null || task.getExcuteTime().getTime() > mainConfig.getFinishedLastDate().getTime()){
            mainConfig.setFinishedLastDate(task.getExcuteTime());
        }

        if(mainConfig.getFinishedTaskCount() == null){
            mainConfig.setFinishedTaskCount(new AtomicLong(0));
        }
        mainConfig.getFinishedTaskCount().incrementAndGet();
    }
}
