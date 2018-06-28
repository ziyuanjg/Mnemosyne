package slave;

import com.alibaba.fastjson.JSON;
import common.BizResult;
import common.Configuration;
import common.httpClient.RequestTypeEnum;
import election.ElectionConfig;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Mr.Luo on 2018/6/27
 */
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
     * 允许连续心跳请求连续失败的最大次数
     */
    private final Integer failMaxNum = 3;

    /**
     * 心跳间隔时间
     */
    private final Long heartTime = 5000L;

    /**
     * 标识本节点为子节点，如果在后续的选举中当选为主节点，则应终止子节点线程
     */
    public Boolean isSlave = Boolean.TRUE;

    /**
     * 子节点主控线程
     */
    class SlaveService implements Runnable {

        @Override
        public void run() {

            // 定时心跳 主节点超时时发起选举
            while (isSlave) {

                try {
                    String result = Configuration.getHttpClient().send(ElectionConfig.getMasterNode().getUrl() + heartUrl,
                            null,
                            null,
                            RequestTypeEnum.GET);

                    BizResult bizResult = JSON.parseObject(result, BizResult.class);
                    if(!bizResult.getSuccess()){
                        failNum.incrementAndGet();
                    }
                } catch (Exception e) {
                    failNum.incrementAndGet();
                }

                if(failNum.get() >= failMaxNum){

                    // 从集群中踢掉当前主节点
                    ElectionConfig.removeNode(ElectionConfig.getMasterNode());
                    // 发起选举
                    Configuration.getElectionService().startElection();
                }

                try {
                    Thread.sleep(heartTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
