package com.mnemosyne.task.disk;

import java.util.concurrent.atomic.AtomicInteger;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * Created by Mr.Luo on 2018/5/16
 */
@Builder
@Data
@AllArgsConstructor
public class FileConfig {

    /**
     * 任务长度
     */
    private Integer taskMAXLength;

    /**
     * 此分片任务数
     */
    private AtomicInteger taskNum;

    /**
     * 此分片已经完成的任务数
     */
    private AtomicInteger finishedTaskNum;


    public Boolean isFinish() {

        if (taskNum == null || finishedTaskNum == null) {
            return Boolean.FALSE;
        }

        if (finishedTaskNum.get() == taskNum.get()) {
            return Boolean.TRUE;
        }

        return Boolean.FALSE;
    }

    public void addTaskNum() {
        taskNum.incrementAndGet();
    }

    public void addFinishedTaskNum() {
        finishedTaskNum.incrementAndGet();
    }

    public Integer getEndTaskId() {
        return taskNum.get();
    }
}
