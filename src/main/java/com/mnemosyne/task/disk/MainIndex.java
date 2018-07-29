package com.mnemosyne.task.disk;

import java.util.Date;
import lombok.Builder;
import lombok.Data;

/**
 * Created by Mr.Luo on 2018/7/17
 */
@Data
@Builder
public class MainIndex {

    private Long Id;

    /**
     * 执行时间
     */
    private Date excuteTime;

    /**
     * 所在分区
     */
    private Integer partation;

    /**
     * 任务在文件中的位置
     */
    private Long fileIndex;
}
