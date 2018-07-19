import com.alibaba.fastjson.JSON;
import com.mnemosyne.task.disk.MainIndex;
import java.util.HashMap;
import com.mnemosyne.task.SaveConfig;
import java.util.Date;
import lombok.Data;
import org.junit.Test;
import com.mnemosyne.common.CallBackTypeEnum;
import com.mnemosyne.task.disk.DiskTaskHandler;
import com.mnemosyne.task.Task;

/**
 * Created by Mr.Luo on 2018/4/26
 */
public class ioTest {

    @Test
    public void diskSaveTask(){

        SaveConfig.setFilePath("/Users/xiluo");

        DiskTaskHandler diskSaveTask = new DiskTaskHandler();

        Date date = new Date();
        Long time1 = System.currentTimeMillis();
        for(Integer i = 0; i < 100; i++){
            Task task = Task.builder()
                    .url(i.toString())
                    .callBackType(CallBackTypeEnum.HTTP)
                    .param(new HashMap<>())
                    .header(new HashMap<>())
                    .excuteTime(date)
                    .build();
            diskSaveTask.saveTask(task);
        }
        Long time2 = System.currentTimeMillis();
        System.out.println("写入花费"+(time2 - time1)+"ms");

        Task task = diskSaveTask.getTask(date,0);

        System.out.println("读取花费"+(System.currentTimeMillis() - time2)+"ms");
    }

    // TODO 任务依赖，如果后面任务依赖前面任务，这个可能需要一些措施来暂停后置任务的执行

    // TODO 任务暂停，暂定可以依靠id，需加一个状态
}
