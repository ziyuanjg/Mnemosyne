package slave;

import com.alibaba.fastjson.JSON;
import common.BizResult;
import common.Configuration;
import common.httpClient.RequestTypeEnum;
import election.ElectionConfig;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Headers;

/**
 * Created by Mr.Luo on 2018/6/27
 */
@Slf4j(topic = "slave")
public class SlaveNodeService {

    /**
     * 心跳接口url
     */
    private final String heartUrl = "master/heart";

    /**
     * 心跳连续失败次数
     */
    private final AtomicInteger failNum = new AtomicInteger(0);

    /**
     * 标识本节点为子节点，如果在后续的选举中当选为主节点，则应终止子节点线程
     */
    public static Boolean isSlave = Boolean.TRUE;

    /**
     * 开启子节点主控线程
     */
    public void startSlaveService(){

        isSlave = Boolean.TRUE;
        Thread thread = new Thread(new SlaveService());
        thread.start();
    }

    /**
     * 停止子节点主控线程
     */
    public void stopSlaveService(){

        isSlave = Boolean.FALSE;
    }

    public Integer getFailNum() {
        return failNum.get();
    }

    /**
     * 子节点主控线程
     */
    class SlaveService implements Runnable {

        @Override
        public void run() {

            // 定时心跳 主节点超时时发起选举
            while (isSlave) {

                try {
                    Headers headers = new Headers.Builder()
                            .add("url", ElectionConfig.getLocalNode().getUrl())
                            .add("time", String.valueOf(System.currentTimeMillis()))
                            .build();
                    String result = Configuration.getHttpClient().send(ElectionConfig.getMasterNode().getUrl() + heartUrl,
                            headers,
                            null,
                            RequestTypeEnum.GET);

                    BizResult bizResult = JSON.parseObject(result, BizResult.class);
                    if(!bizResult.getSuccess()){
                        failNum.incrementAndGet();
                    }
                } catch (Exception e) {
                    failNum.incrementAndGet();
                }

                if(failNum.get() >= SlaveConfig.getFailMaxNum()){

                    // 从集群中踢掉当前主节点
//                    ElectionConfig.removeNode(ElectionConfig.getMasterNode());
//                    ElectionConfig.setMasterNode(null);
                    // 发起选举
                    Configuration.getElectionService().startElection();
                    stopSlaveService();
                }

                try {
                    Thread.sleep(SlaveConfig.getHeartTime());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
