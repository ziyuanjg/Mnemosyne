package task;

import lombok.Builder;
import lombok.Data;

/**
 * Created by Mr.Luo on 2018/5/16
 */
@Builder
@Data
public class FileConfig {

    /**
     * 任务长度
     */
    private Integer taskMAXLength;

    /**
     * 此分片数据的起始id
     */
    private Integer startId;

    /**
     * 此分片数据的结束id
     */
    private Integer endId;

    /**
     * 此分片已经完成的任务数
     */
    private Integer finishedTask;

}
