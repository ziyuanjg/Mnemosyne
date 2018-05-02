import com.alibaba.fastjson.JSONObject;
import config.SaveConfig;
import java.util.Date;
import org.junit.Test;
import timeWheel.CallBackTypeEnum;
import timeWheel.task.DiskSaveTask;
import timeWheel.task.Task;

/**
 * Created by 希罗 on 2018/4/26
 */
public class ioTest {

    @Test
    public void diskSaveTask(){

        SaveConfig.setFilePath("/Users/xiluo");

        DiskSaveTask diskSaveTask = new DiskSaveTask();

        Date date = new Date();
        Long time1 = System.currentTimeMillis();
        for(Integer i = 0; i < 100000; i++){
            Task task = Task.builder()
                    .url(i.toString())
                    .callBackType(CallBackTypeEnum.HTTP)
                    .param(new JSONObject())
                    .header("header")
                    .excuteTime(date)
                    .build();
            diskSaveTask.saveTask(task);
        }
        Long time2 = System.currentTimeMillis();
        System.out.println("写入花费"+(time2 - time1)+"ms");

        Task task = diskSaveTask.getTask(date,0);

        System.out.println("读取花费"+(System.currentTimeMillis() - time2)+"ms");
    }
}
