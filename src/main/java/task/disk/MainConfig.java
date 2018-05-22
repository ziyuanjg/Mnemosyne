package task.disk;

import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * Created by Mr.Luo on 2018/5/21
 */
@Data
@Builder
@AllArgsConstructor
public class MainConfig {

    /**
     * 任务数
     */
    private AtomicLong taskCount;

    /**
     * 已完成任务数
     */
    private AtomicLong finishedTaskCount;

    /**
     * 最后执行的时间片
     */
    private Date finishedLastDate;



}
