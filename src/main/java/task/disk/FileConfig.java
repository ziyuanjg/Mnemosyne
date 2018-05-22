package task.disk;

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
     * 此分片数据的起始id
     */
    private Integer startId;

    /**
     * 此分片数据的结束id
     */
    private AtomicInteger endId;

    /**
     * 此分片已经完成的任务数
     */
    private AtomicInteger finishedTask;


    public Boolean isFinish(){

        if(startId == null || endId == null || finishedTask == null){
            return Boolean.FALSE;
        }

        if(finishedTask.get() == (endId.get() - startId + 1)){
            return Boolean.TRUE;
        }

        return Boolean.FALSE;
    }
}
