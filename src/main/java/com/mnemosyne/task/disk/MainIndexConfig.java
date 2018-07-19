package com.mnemosyne.task.disk;

import java.util.concurrent.atomic.AtomicLong;
import lombok.Builder;
import lombok.Data;

/**
 * Created by Mr.Luo on 2018/7/17
 */
@Data
@Builder
public class MainIndexConfig {

    /**
     * 任务数量
     */
    private AtomicLong taskNum;

    /**
     * 最后一条id
     */
    private AtomicLong endId;


    public Long addTask(){

        taskNum.incrementAndGet();

        return endId.incrementAndGet();
    }

    public Long getTaskNum() {
        return taskNum.get();
    }

    public Long getEndId() {
        return endId.get();
    }
}
