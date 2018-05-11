import java.io.File;
import java.util.HashMap;
import task.SaveConfig;
import java.util.Date;
import org.junit.Test;
import task.CallBackTypeEnum;
import task.DiskTaskHandler;
import task.Task;

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
        for(Integer i = 0; i < 100000; i++){
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

@Test
    public void ttt(){

        File file = new File("/Users/xiluo/2018-01-01/012/2018-01-01 01:10:10");
        if(!file.getParentFile().exists()){
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
