package master.assign;

import election.ElectionConfig;
import election.ServiceNode;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * 负载均衡策略 Created by Mr.Luo on 2018/5/10
 */
public interface LoadStrategy {

    /**
     * 选举执行本次任务的节点
     */
    ServiceNode getSlaveNode();

    class DefaultLoadStrategy implements LoadStrategy {

        @Override
        public ServiceNode getSlaveNode() {

            Set<ServiceNode> serviceNodeList = ElectionConfig.getServiceNodeList();

            return (ServiceNode) serviceNodeList.toArray()[new Random().nextInt(serviceNodeList.size())];
        }
    }
}
