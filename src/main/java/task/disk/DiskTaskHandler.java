package task.disk;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import task.AbstractTaskHandler;
import task.SaveConfig;
import task.Task;
import task.exception.FileException;
import task.exception.TaskExceptionEnum;

/**
 * 小文件分片持久化任务 Created by Mr.Luo on 2018/4/28
 */
public class DiskTaskHandler extends AbstractTaskHandler {

    private final FileUtil fileUtil = new FileUtil();

    private final Integer FILE_MAX_LENGTH =
            (SaveConfig.getTaskMAXLength() * SaveConfig.getTaskNumOfPartition()) + FileUtil.FILE_CONFIG_LENGTH;

    private final MainConfig mainConfig = fileUtil.getMainConfig();


    @Override
    protected Boolean save(Task task) {

        if (task == null) {
            return Boolean.FALSE;
        }

        if (task.getIsFinished()) {

            Integer id = task.getId();
            File file;
            String fileName;
            Integer fileNum;
            DateTime excuteTime = DateUtil.date(task.getExcuteTime());
            String taskDate = DateUtil.formatDateTime(task.getExcuteTime());
            if (id % SaveConfig.getTaskNumOfPartition() > 0) {

                fileNum = id / SaveConfig.getTaskNumOfPartition();
                fileName = taskDate + "." + fileNum;
                String filePath = fileUtil.getFilePath(excuteTime);
                file = new File(filePath + fileName);
            } else {

                fileNum = (id / SaveConfig.getTaskNumOfPartition()) - 1;
                fileName = taskDate + "." + fileNum;
                String filePath = fileUtil.getFilePath(excuteTime);
                file = new File(filePath + fileName);
            }

            FileConfig fileConfig = fileUtil.getFileConfig(file);
            if (fileConfig.getStartId() > task.getId() || (fileConfig.getStartId() + SaveConfig.getTaskNumOfPartition()) < task.getId()) {
                throw new FileException(TaskExceptionEnum.FILE_PARTITION_ERROR);
            }

            Long startIndex =
                    FileUtil.FILE_CONFIG_LENGTH.longValue() + ((task.getId() - fileConfig.getStartId()) * SaveConfig
                            .getTaskMAXLength());

            try {
                fileUtil.getFileLock(fileName);
                fileUtil.SaveTaskToFile(task, file, fileNum, startIndex);

                fileConfig.getFinishedTask().incrementAndGet();
                fileUtil.setFileConfig(file, fileConfig);

                if(mainConfig.getFinishedLastDate() == null || task.getExcuteTime().getTime() > mainConfig.getFinishedLastDate().getTime()){
                    mainConfig.setFinishedLastDate(task.getExcuteTime());
                }
                mainConfig.getFinishedTaskCount().incrementAndGet();
                fileUtil.setMainConfig(mainConfig);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                fileUtil.releaseFileLock(fileName);
            }
        } else {

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

            try {
                fileUtil.getFileLock(fileName);
                fileUtil.SaveTaskToFile(task, file, fileNum, null);

                FileConfig fileConfig = fileUtil.getFileConfig(file);

                fileConfig.getEndId().incrementAndGet();
                fileUtil.setFileConfig(file, fileConfig);

                mainConfig.getTaskCount().incrementAndGet();
                fileUtil.setMainConfig(mainConfig);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                fileUtil.releaseFileLock(fileName);
            }
        }

        return Boolean.TRUE;
    }

    @Override
    protected Task get(Date date, Integer partition) {

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
    protected Integer getPartitionNum(Date date) {

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
    protected List<Task> getUnFinishedTaskIds(Date date) {

        Date lastRunDate = mainConfig.getFinishedLastDate();

        if(lastRunDate.getTime() >= date.getTime()){
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

                            if(!task.getIsFinished()){
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
}
