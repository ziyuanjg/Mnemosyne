package master.receive;

import timeWheel.task.Task;

/**
 * Created by 希罗 on 2018/5/3
 */
public interface ReceiveTask {

    /**
     * 接收任务消息
     */
    Task receveTask();
}
