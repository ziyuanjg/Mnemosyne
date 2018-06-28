package master;

import common.BizResult;
import election.ElectionConfig;
import election.ServiceNode;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import lombok.extern.slf4j.Slf4j;
import slave.SlaveConfig;

/**
 * Created by Mr.Luo on 2018/5/23
 */
@Slf4j(topic = "slave")
@Path("master")
public class MasterNodeService {

    /**
     * 最近心跳记录
     */
    private final Map<ServiceNode, Long> heartMap = new HashMap<>();

    /**
     * 标志是否为主节点
     */
    private static Boolean isMaster = Boolean.FALSE;

    @Path("heart")
    @GET
    public BizResult heart(@Context HttpServletRequest request) {


        String url = request.getHeader("url");

        Long time = Long.valueOf(request.getHeader("time"));

        ServiceNode serviceNode = ElectionConfig.getServiceNodeByUrl(url);

        if(heartMap.get(serviceNode) == null && heartMap.get(serviceNode) < time){
            heartMap.put(serviceNode, time);
        }

        return BizResult.createSuccessResult(null);
    }

    /**
     * 开启心跳检测
     */
    public void startHeartThread(){

        isMaster = Boolean.TRUE;
        Thread thread = new Thread(new HeartThread());
        thread.start();
    }

    /**
     * 停止心跳检测
     */
    public void stopHeartThread(){
        isMaster = Boolean.FALSE;
    }

    /**
     * 心跳检测线程
     */
    class HeartThread implements Runnable{

        @Override
        public void run() {

            while (isMaster){
                // 检测是否连续两次心跳未收到
                for(Entry<ServiceNode, Long> entry : heartMap.entrySet()){
                    if((System.currentTimeMillis() - entry.getValue()) > (SlaveConfig.getHeartTime() * SlaveConfig.getFailMaxNum())){
                        ElectionConfig.removeNode(entry.getKey());
                    }
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
