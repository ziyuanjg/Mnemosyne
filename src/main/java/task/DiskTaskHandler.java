package task;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import java.io.File;
import java.util.Date;
import task.exception.FileException;
import task.exception.TaskExceptionEnum;

/**
 * 小文件分片持久化任务 Created by Mr.Luo on 2018/4/28
 */
public class DiskTaskHandler extends AbstractTaskHandler {

    private final FileUtil fileUtil = new FileUtil();

    private final Integer FILE_MAX_LENGTH =
            (SaveConfig.getTaskMAXLength() * SaveConfig.getTaskNumOfPartition()) + FileUtil.FILE_CONFIG_LENGTH;

    @Override
    Boolean save(Task task) {

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
            if (fileConfig.getStartId() > task.getId() || (fileConfig.getStartId() + 1000) < task.getId()) {
                throw new FileException(TaskExceptionEnum.FILE_PARTITION_ERROR);
            }

            Long startIndex =
                    FileUtil.FILE_CONFIG_LENGTH.longValue() + ((task.getId() - fileConfig.getStartId()) * SaveConfig
                            .getTaskMAXLength());

            try {
                fileUtil.getFileLock(fileName);
                fileUtil.SaveTaskToFile(task, file, fileNum, startIndex);
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
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                fileUtil.releaseFileLock(fileName);
            }
        }

        return Boolean.TRUE;
    }

    @Override
    Task get(Date date, Integer partition) {

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
    Integer getPartitionNum(Date date) {

        DateTime excuteTime = DateUtil.date(date);
        String filePath = fileUtil.getFilePath(excuteTime);

        File file = new File(filePath);

        if (file.isDirectory()) {
            return file.listFiles().length;
        } else {
            return 0;
        }
    }

}
