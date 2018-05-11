package master.assign;

import electon.ServiceNode;

/**
 * 负载均衡策略
 * Created by Mr.Luo on 2018/5/10
 */
public interface LoadStrategy {

    /**
     * 选举执行本次任务的节点
     * @return
     */
    ServiceNode getSlaveNode();

}
